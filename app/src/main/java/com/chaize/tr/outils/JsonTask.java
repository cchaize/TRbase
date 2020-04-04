package com.chaize.tr.outils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.chaize.tr.controleur.Controle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JsonTask extends AsyncTask<String, String, String> {

    private Context context;
    private ProgressDialog pd;
    private TextView result;
    private String error = "";

    public JsonTask(Context context, TextView res) {
        super();
        this.context = context;
        this.result = res;
    }

    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(context);
        pd.setMessage("Please wait");
        pd.setCancelable(false);
        pd.show();
    }

    protected String doInBackground(String... params) {


        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();


            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
                //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.doInBackground : " + e.toString());
            error = e.getLocalizedMessage();
        } catch (IOException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.doInBackground : " + e.toString());
            error = e.getLocalizedMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.doInBackground : " + e.toString());
                error = e.getLocalizedMessage();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        String cles[] = {"product_name_fr", "product_name", "generic_name_fr", "generic_name", "ingredients_text_fr", "ingredients_text"};

        if (pd.isShowing()) {
            pd.dismiss();
        }
        if (error.equals("")) {
            String marque = "";
            String text = "";
            try {
                if (result != null) {
                    JSONObject json = new JSONObject(result);
                    if (json.has("product")) {
                        json = json.getJSONObject("product");
                        int i = 0;
                        while (text.equals("")) {
                            if (json.has(cles[i]))
                                text = json.getString(cles[i]);
                            i++;
                        }
                        if (json.has("brands"))
                            marque = json.getString("brands");
                    }
                }
            } catch (JSONException e) {
                Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.onPostExecute : " + e.toString());
            }
            if (text.equals(""))
                text = "Produit inconnu";
            this.result.setText(marque + " - " + text);
        } else {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
    }
}
