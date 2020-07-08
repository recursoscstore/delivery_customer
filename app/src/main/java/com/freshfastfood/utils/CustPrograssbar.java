package com.freshfastfood.utils;

import android.app.ProgressDialog;

public class CustPrograssbar {
    public static ProgressDialog progressDialog;

    public void PrograssCreate() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                return;
            } else {
                progressDialog.setMessage("Cargando...");
                progressDialog.show();
            }
        } catch (Exception e) {
            e.toString();
        }
    }

    public void ClosePrograssBar() {
        if (progressDialog != null) {
            try {
                progressDialog.cancel();
            } catch (Exception e) {
                e.toString();
            }
        }

    }
}
