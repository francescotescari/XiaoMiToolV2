package com.xiaomitool.v2.test;

import com.xiaomitool.v2.logging.Log;

import java.nio.charset.Charset;

public class Test {
    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla ut consectetur justo. Sed lobortis lectus id erat laoreet aliquet. Vestibulum tortor felis, venenatis eget sem id, accumsan molestie justo. Ut efficitur massa lorem. Donec pretium tortor purus, a iaculis nunc feugiat vitae. Sed ullamcorper dui eget ipsum maximus volutpat. Nulla facilisi. Praesent lacinia tincidunt velit vitae pellentesque. Ut luctus sollicitudin iaculis.\n" +
            "\n" +
            "Nullam egestas lacus diam, malesuada rhoncus nisl dignissim eu. Nulla facilisi. Maecenas dignissim suscipit eros, quis elementum leo maximus ac. Nulla eu ipsum ullamcorper, porta purus vel, fringilla dolor. Aliquam lectus arcu, commodo sit amet augue eu, sagittis condimentum tellus. Suspendisse semper lectus vitae magna fringilla porttitor. Nulla diam ante, hendrerit in suscipit vel, pellentesque eu neque. Praesent eu molestie lorem. Nulla facilisi. Ut hendrerit vitae leo vel maximus.\n" +
            "\n" +
            "Mauris commodo lectus id mattis posuere. Quisque ut tincidunt ante. Maecenas laoreet rhoncus eros. Sed in dolor vel erat rhoncus mattis. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Phasellus ut sagittis ex. Donec quis orci nec odio tempor venenatis vehicula et turpis. Phasellus luctus egestas mauris a cursus. Suspendisse potenti. Nunc vel urna porttitor, eleifend nunc id, fermentum elit. Vestibulum malesuada sem est, rhoncus euismod risus rhoncus eget. Morbi mattis tristique nibh. Curabitur id odio vel mi rutrum suscipit ut sed nisl. Duis commodo eget dolor ut congue. Fusce id sapien sed arcu scelerisque auctor sed non mi. Vivamus eu libero et urna ullamcorper ultrices et sed sapien.\n" +
            "\n" +
            "Cras nunc lacus, finibus vel dui id, sollicitudin vestibulum nulla. Phasellus tellus magna, hendrerit vel tempus a, tempus at urna. Pellentesque pellentesque efficitur rhoncus. Aliquam vitae neque massa. Donec auctor id augue et ullamcorper. Aliquam tincidunt sem vel augue interdum rhoncus. Aenean iaculis, neque at ultricies maximus, augue risus volutpat ex, a pulvinar arcu sem et massa. Duis auctor varius arcu, nec posuere lorem pellentesque vitae. Curabitur blandit semper quam, non sollicitudin enim blandit vel. Mauris ac nisl vel ipsum maximus luctus. In posuere, lectus in commodo auctor, nulla urna imperdiet ex, ac iaculis purus ligula quis diam. Curabitur semper id odio ut sollicitudin. Sed ut nulla nec est luctus viverra vel ac urna. Sed ullamcorper pharetra massa in cursus.\n" +
            "\n" +
            "Nunc odio dolor, posuere id luctus nec, lobortis vitae lectus. Vivamus mauris lectus, interdum at maximus non, blandit id odio. Aliquam eu est maximus, molestie nibh auctor, posuere sapien. Praesent suscipit dictum tellus. Nunc finibus, justo id volutpat dignissim, neque magna aliquam lacus, in mollis lectus enim in libero. Cras mauris neque, euismod eget fringilla ut, mollis vitae nunc. Integer gravida ante quis mauris imperdiet, sit amet bibendum velit tincidunt. Pellentesque hendrerit ex dui, sit amet eleifend libero porta ac. Nulla fermentum mauris id erat euismod, pharetra sodales dolor tincidunt. Donec eget erat facilisis, volutpat ex eu, lobortis ligula. Proin pretium erat tristique lectus semper, vitae aliquet nibh interdum. Phasellus suscipit porta nunc, vel aliquam sapien mattis dignissim. Nunc quis risus ac turpis condimentum congue. Nulla at lacinia est, ac commodo dolor. Ut luctus sit amet nulla eget tempor.";

   static class Troll {
       private int hc;
       public Troll(int hc){
           this.hc = hc;
       }
       @Override
       public int hashCode() {
           return hc;
       }

       @Override
       public boolean equals(Object obj) {
           return true;
       }
   }


    public static void main(String[] argsv){
        /*Log.debug(Charset.defaultCharset().toString());*/
    }




    public static final String L = "ABBA98A3D2D6C7D0A4BFEECA4365CB9AA7E9E709E5F177F564F3314115D62179B412179DC28C9EBE4CC58C2D27B89EDC7EFC0DDC084D3C03C2456EBD98F2E3FB90046CE0A0577EDC1BD248085CC2D9BAF61C940D2A4BF004AB587C8029CB4E85BAF214D78F9C91D77E7E950656E8AA55BA57FDF2C3223BA5D6649559B8B06A3CEBFE627561E82749DFA1B9B345248444E46EA0BA07B817596784BB630F41EC1756894B6FA00086403EC726FDF4133B3AB59ED3DCDBB99D5E3A5EB4AEEFF74C338F2C93485C7CDEE07912B43C8377240F39F16A1E21077E3C7C69D92820D77853EB550D57601EC2F721DA18D5A474CB6135091F23FA0768B2E2B3F141FE0DA749";


}
