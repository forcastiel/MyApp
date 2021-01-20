package com.shrek.klib.retrofit.handler;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * @author shrek
 * @date: 2016-04-21
 */
public class DialogHandler<Bo> implements RestHandler<Bo>{

    Context context;
    String title;
    String message;

    ProgressDialog dialog;

    public DialogHandler(Context context, String title, String message) {
        this.context = context;
        this.title = title;
        this.message = message;
    }

    @Override
    public void pre() {
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(true);
        dialog.show();
    }


    private void hide(){
        try{
            dialog.dismiss();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            dialog = null;
        }
    }

    @Override
    public void post(Bo bo) {
        hide();
    }

    @Override
    public void error(Throwable throwable) {
        hide();
    }
}
