package com.xiaomitool.v2.test;

import com.xiaomitool.v2.logging.Log;

import com.xiaomitool.v2.utility.MTPUtils;
import com.xiaomitool.v2.utility.Pointer;

import java.util.Map;

public class Main3 {
    public static void main(String[] argv) throws Exception {

        Map <String, MTPUtils.MTPDevice>map = MTPUtils.list();
        for (Map.Entry<String, MTPUtils.MTPDevice> e: map.entrySet()){
            /*Task t = MTPUtils.getPushTask(e.getValue(), Paths.get("C:\\XiaoMi\\XiaoMiTool\\rom\\whyred_global_images_8.7.12_20180712.0000.00_8.1_global_e775b9c0d5.tgz"), "/");

            TaskManager.getInstance().startSameThread(t);*/
            Log.debug(e.getKey());
        }
        Pointer p = new Pointer();
       /* new ProcedureRunner(new GuiListener.Debug()).run(new RInstall(StockRecoveryInstall.enableMtp(new Device("88eec8b5"), p), StockRecoveryInstall.sendFileViaMTP((MTPUtils.MTPDevice) p.pointed, Paths.get("F:\\Download\\sdattest\\whyred_images_8.9.6_20180906.0000.00_8.1_cn_efc57c7ee22\\whyred_images_8.9.6_20180906.0000.00_8.1_cn\\images\\mdtp.img"))) {
            @Override
            public void run(ProcedureRunner procedureRunner) throws InstallException, InterruptedException {

            }
        });*/
        Log.debug(p.pointed);

       /* HungyThread thread = new HungyThread();
        thread.start();
        Thread.sleep(2000);
        thread.addRunnable(new Runnable() {
            @Override
            public void run() {
                Log.debug("Dopo due secondi");
            }
        });
        Thread.sleep(4000);
        thread.addRunnable(() -> {
            Log.debug("Dopo sei secondi");
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.debug("ADDING RUNNABLE");
            try {
                thread.addRunnable(new Runnable() {
                    @Override
                    public void run() {

                        Log.debug("Dopo 12");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.debug("Dopo 10 secondi");
        });
*/
    }
}
