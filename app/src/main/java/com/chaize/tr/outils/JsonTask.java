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
    private String error="";

    public JsonTask(Context context, TextView res){
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
                buffer.append(line+"\n");
                //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

            }

            return buffer.toString();


        } catch (MalformedURLException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.doInBackground : "+e.toString());
            error = e.getLocalizedMessage();
        } catch (IOException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.doInBackground : "+e.toString());
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
                Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.doInBackground : "+e.toString());
                error = e.getLocalizedMessage();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (pd.isShowing()){
            pd.dismiss();
        }
        if (error.equals("")) {
            String text = "Produit inconnu";
            try {
                if (result != null) {
                    JSONObject json = new JSONObject(result);
                    text = json.getJSONObject("product").getString("generic_name");
                    if (text.equals("")) {
                        text = json.getJSONObject("product").getString("ingredients_text_fr");
                        if (text.equals("")) {
                            text = json.getJSONObject("product").getString("ingredients_text");
                        }
                    }
                }
            } catch (JSONException e) {
                Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "JsonTask.onPostExecute : " + e.toString());
            }
            this.result.setText(text);
        } else {
            Toast.makeText(context,error,Toast.LENGTH_LONG).show();
        }
    }
}
