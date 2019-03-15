package com.xiaomitool.v2.test;

import com.xiaomitool.v2.inet.CustomHttpException;
import com.xiaomitool.v2.logging.Log;
import com.xiaomitool.v2.rom.MiuiRom;
import com.xiaomitool.v2.rom.MiuiZipRom;
import com.xiaomitool.v2.xiaomi.XiaomiKeystore;
import com.xiaomitool.v2.xiaomi.XiaomiProcedureException;
import com.xiaomitool.v2.xiaomi.miuithings.Branch;
import com.xiaomitool.v2.xiaomi.miuithings.DefaultRequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.DeviceRequestParams;
import com.xiaomitool.v2.xiaomi.miuithings.RequestParams;
import com.xiaomitool.v2.xiaomi.romota.MiuiRomOta;
import com.xiaomitool.v2.xiaomi.unlock.UnlockCommonRequests;
import com.xiaomitool.v2.xiaomi.unlock.UnlockRequest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Main {
   //public static final int Start = 3428000;
    public static final int Start = 3351900;
    public static int scarto = 0;
    public static int numThread = 10;
    public static void main(String[] argv) throws IOException, XiaomiProcedureException, CustomHttpException {

        /*if (argv.length < 2){
            System.err.println("Usage: MiOtaUpdates.jar passToken userId");
            System.exit(1);
        }*/
        //String aargv[] = new String[]{"V1:isBbO7qT2fhtbwL9sYAWOw1F/3NxY9rEGd/Rpl90v+8g3MPwBcGj+LhM7gm2KKdML2d2Z/ubuxQVmZzlyNSjzunw7/coc/ayIf3HCqBZ9Dxg5EURCySg6KexJHox4JTkNOfLrsKEAIue8xdbjH+d5srzS7oSw1feUoLmo89t9WPye0e2w2UG/MJs+YxW+tBTLOhgbnVz0H9nXBcWQayyZwwuNOiB5HPbfjBX2kM6IBxuHA7cdDRWjSDkDMfVwvch", "513820086"};
        //String aar
        String gv[] = new String[]{"V1:sYLMdrtmr68+XYlt3KEGMvIK33el49rcewgc/Aj2eDbhlxtUYYoC403jFphGxbILHMBOxCZ8jb09lYio/ueuyNEA2FG5GGTYdcWWcK09ydkY/wBzMoVhriDnVRguxt7Qj+Xn52Lo2RbErRDknI35gYeIN4gWRpDGwDeL9qTo8ODgyOftZSYklvth66lqzghIiGBiLUu+suwcbaQ/wh+sZostweENZGAfGkRGNF8FsD0VgBHsi+X5xKFoh+FndaZ1", "1606054557"};
        gv = new String[]{"V1:UzVU8KYgKUF+aP6f4QkYe4nJRAa4IB4o9a7MjeCocT+PwZw8pVAI1B0a8atv9RktplQq+EFYLvU+swKN2705sjUXC3nGlb2blWkFgUYFtqrWhxPaM2NLUysFwD5fakuzmDKQAREQK5KrtLVt0nSBRrOi1gkqUyOwn42Pe/TsY6R1oEKqMjtB+0gQJvXe67kECSWaGRM6YLO3SrHIZwfxyViNMLihMppHznq9yWQJfE8bAk96Tsk5N6uZMk69BeVs","1606054557"};


        XiaomiKeystore keystore = XiaomiKeystore.getInstance();
        keystore.setCredentials(gv[1], gv[0]);
        //*keystore.requireServiceKeyAndToken("miuiromota");
        RequestParams params = new DeviceRequestParams("jason_global","V10.1.2.0.NCHMIFI","7.1",Branch.STABLE,"0xb7f63ec7",2);
        /*params.setPkg("d8f3965aebf2dbfb0291fb21be6cff1b");
        MiuiRomOta.otaV3_request(params);
        if (true){
            return;
        }*/
        scarto = 0;
        //Log.debug(UnlockCommonRequests.test());VQEBIQEQdAsBSujVP9OjWVoBu7zFTwMHY2VwaGV1cwIEQXtF7A==

        //Log.debug(UnlockCommonRequests.ahaUnlock("VQEBMwEgdAsBSujVP9OjWVoBu7zFT3QLAUro1T/To1laAbu8xU8DCWJlcnlsbGl1bQIEQXtF7A==","dipper","","",""));
        Log.debug(UnlockCommonRequests.ahaUnlock("VQEBIQEQRbgeQ5xHgGiytTxvaushBQMHZGF2aW5jaQIENrrqAw==","scorpio","","","")); //Please unlock 49 hours later
        //Log.debug(UnlockCommonRequests.ahaUnlock("VQEBIAEQdAsBSujVP9OjWVoBu7zFTwMGZGlwcGVyAgRBe0Xs","dipper","","",""));//Please unlock 337 hours later

        if (true){
            return;
        }
        MiuiRomOta.otaV3_request(new DefaultRequestParams("perseus","8.11.28","9.0"));
        if (true){
            return;
        }
        for (int i = 0; i<numThread; ++i){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int sc = scarto;
                    ++scarto;
                    for (int i = Start+sc; true; i+=numThread) {
                        String num = String.valueOf(i);
                        RequestParams requestParams = new DefaultRequestParams("miflash_pro", num.substring(0,1)+"."+num.substring(1,2)+"."+num.substring(2,5)+"."+num.substring(5), "4.0");
                        HashMap<MiuiRom.Kind, MiuiZipRom> res = null;
                        try {
                            res = MiuiRomOta.otaV3_request(requestParams);
                        } catch (Exception ignored) {
                            continue;
                        }
                        if (res.size() != 0) {
                            Log.debug("SIIZE:" + res.size());
                            System.exit(0);
                        }
                    }
                }
            }).start();
            try {
                Thread.sleep(2000/numThread);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = Start+1; true; i+=4) {
                    String num = String.valueOf(i);
                    RequestParams requestParams = new DefaultRequestParams("miflash_unlock", num.substring(0,1)+"."+num.substring(1,2)+"."+num.substring(2,5)+"."+num.substring(5), "4.0");
                    HashMap<MiuiRom.Kind, MiuiZipRom> res = null;
                    try {
                        res = MiuiRomOta.otaV3_request(requestParams);
                    } catch (Exception ignored) {
                        continue;
                    }
                    if (res.size() != 0) {
                        Log.debug("SIIZE:" + res.size());
                        System.exit(0);
                    }
                }
            }
        }).start();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = Start+2; true; i+=4) {
                    String num = String.valueOf(i);
                    RequestParams requestParams = new DefaultRequestParams("miflash_unlock", num.substring(0,1)+"."+num.substring(1,2)+"."+num.substring(2,5)+"."+num.substring(5), "4.0");
                    HashMap<MiuiRom.Kind, MiuiZipRom> res = null;
                    try {
                        res = MiuiRomOta.otaV3_request(requestParams);
                    } catch (Exception ignored) {
                        continue;
                    }
                    if (res.size() != 0) {
                        Log.debug("SIIZE:" + res.size());
                        System.exit(0);
                    }
                }
            }
        }).start();
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = Start+3; true; i+=4) {
                    String num = String.valueOf(i);
                    RequestParams requestParams = new DefaultRequestParams("miflash_unlock", num.substring(0,1)+"."+num.substring(1,2)+"."+num.substring(2,5)+"."+num.substring(5), "4.0");
                    HashMap<MiuiRom.Kind, MiuiZipRom> res = null;
                    try {
                        res = MiuiRomOta.otaV3_request(requestParams);
                    } catch (Exception ignored) {
                        continue;
                    }
                    if (res.size() != 0) {
                        Log.debug("SIIZE:" + res.size());
                        System.exit(0);
                    }
                }
            }
        }).start();

*/
        if (true){
            return;
        }
        RequestParams requestParams = null;
        MiuiRomOta.latestTest(requestParams);{
            if (true){
                return;
            }
        }

        requestParams.setPkg("a0229d2921594e6ced60d78754524efc");
        MiuiRomOta.otaV3_request(requestParams);
        if (true){
            return;
        }

        try {
            keystore.requireServiceKeyAndToken("miuiromota");
        } catch (XiaomiProcedureException e) {
            e.printStackTrace();
            System.exit(2);
        } catch (CustomHttpException e) {
            e.printStackTrace();
            System.exit(3);
        }
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Input request data: device miuiVersion androidVersion [branch]");
        while (true) {
            System.out.print("request> ");
            String line = buffer.readLine();
            String[] words = line.split("\\s+");
            if (words.length < 3) {
                System.err.println("Input at least 3 parameter!");
                continue;
            }
            RequestParams params2 = new DeviceRequestParams(words[0], words[1], words[2], words.length > 3 ? Branch.fromCode(words[3]) : null, "0x1c01702d", 2);
            if (words.length > 4){
                params.setPkg(words[4]);
            }
            try {
                Log.debug(MiuiRomOta.otaV3_request(params).toString());
            } catch (XiaomiProcedureException e) {
                e.printStackTrace();
            }
        }
        //keystore.setCredentials("1606054557","V1:HzW8ovuUt6U+f6kJqUrMNZM3r6TuRSe2jgLLXMPMiP61yN4DiTWRU8UfFoEMlcHf8CHzFw/G0cJM3gcPlCWDAMYH8XjoUmpa8HP/6xVSFYXF7gs0cPWVxWsmYScZwKRoCmz40TP0mT9/qnyu7CcvGp1nejcF5YBxvQF97VNnuEdxHnnofBGJiO1wcwVYc1+Rhz/nEB0wKFJ2YhG9Mn5VUA16HFLtRojPEX599zoEXNHETvYHnD4xHkY875Lv1nhk");
        //Log.debugArray(keystore.requireServiceKeyAndToken("miuiromota"));
        //Log.debugArray(keystore.requireServiceKeyAndToken("unlockApi"));
        /*RequestParams params = new DefaultRequestParams("dipper","8.5.30","8.1");
        MiuiRomOta.otaV3_request(params);
        RequestParams params2 = new DefaultRequestParams("dipper","8.6.12","8.1");
        MiuiRomOta.otaV3_request(params2);
        RequestParams params3 = new DefaultRequestParams("jason","8.6.12","8.0", Branch.STABLE);
        MiuiRomOta.otaV3_request(params3);*/


    }


}

