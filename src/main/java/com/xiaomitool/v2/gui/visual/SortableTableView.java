package com.xiaomitool.v2.gui.visual;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.*;

import java.util.function.Function;

public class SortableTableView<S> extends TableView<S> {
    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");

    public SortableTableView(TableViewColumn<S>... columns) {
        ObservableList<TableColumn<S, ?>> cols = getColumns();
        for (TableViewColumn column : columns) {
            cols.add(createCol(column.name, column.mapper, column.size));
        }
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setFocusTraversable(false);
        focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue != null && newValue) {
                    getParent().requestFocus();
                }
            }
        });
        setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });
            row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (MouseButton.SECONDARY.equals(event.getButton()) && !row.isEmpty()) {
                        getItems().remove(row.getItem());
                    }
                }
            });
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != (Integer) db.getContent(SERIALIZED_MIME_TYPE)) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    S draggedPerson = getItems().remove(draggedIndex);
                    int dropIndex;
                    if (row.isEmpty()) {
                        dropIndex = getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }
                    getItems().add(dropIndex, draggedPerson);
                    event.setDropCompleted(true);
                    getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });
            return row;
        });
    }

    private static <T> TableColumn<T, String> createCol(String title,
                                                        Function<T, ObservableValue<String>> mapper, Double size) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> mapper.apply(cellData.getValue()));
        if (size != null) {
            col.setPrefWidth(size);
        }
        col.setSortable(false);
        return col;
    }

    public static class TableViewColumn<T> {
        private final String name;
        private final Function<T, ObservableValue<String>> mapper;
        private final Double size;

        public TableViewColumn(String name, Function<T, ObservableValue<String>> mapper) {
            this(name, mapper, null);
        }

        public TableViewColumn(String name, Function<T, ObservableValue<String>> mapper, Double size) {
            this.name = name;
            this.mapper = mapper;
            this.size = size;
        }
    }
}
