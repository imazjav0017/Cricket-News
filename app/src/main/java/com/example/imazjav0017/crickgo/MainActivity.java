package com.example.imazjav0017.crickgo;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> titles=new ArrayList<>();
    ArrayList<String> content=new ArrayList<>();
    SQLiteDatabase articlesDb;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView=(ListView)findViewById(R.id.listView);

        adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,titles);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),WebSite.class);
                intent.putExtra("Content",content.get(position));
                startActivity(intent);
            }
        });
        articlesDb=this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        articlesDb.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY,title VARCHAR,content VARCHAR)");
        downloader obj=new downloader();
        obj.execute("https://newsapi.org/v1/articles?source=espn-cric-info&sortBy=" +
                "latest&apiKey=86c2957396ab497bbb0bb8458dbc15f3");
        updateList();

        listView.setAdapter(adapter);
    }
    public void updateList()
    {
        Cursor c=articlesDb.rawQuery("SELECT * FROM articles",null);
        int titleIndex=c.getColumnIndex("title");
        int contentIndex=c.getColumnIndex("content");
        if(c.moveToFirst())
        {
            titles.clear();
            content.clear();
            Log.i("info-----",c.getString(titleIndex));
            do {

                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));
            }while(c.moveToNext());

            adapter.notifyDataSetChanged();
        }
    }
    public class downloader extends AsyncTask<String,Void,String>{
        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection connection=null;
            try {
                url=new URL(params[0]);
                connection=(HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream in=connection.getInputStream();
                InputStreamReader reader=new InputStreamReader(in);
                String result="";
                int data=reader.read();
                while(data!=-1)
                {
                    char current=(char)data;
                    result+=current;
                    data=reader.read();
                }
                JSONObject jsonObject=new JSONObject(result);
                String articles=jsonObject.getString("articles");
                JSONArray jsonArray=new JSONArray(articles);
                articlesDb.execSQL("DELETE FROM articles");
                for(int index=0;index<jsonArray.length();index++)
                {
                    String title = jsonArray.getJSONObject(index).getString("title");
                    String urlSite = jsonArray.getJSONObject(index).getString("url");
                    Log.i("info----",Integer.toString(index));
                    if(title!= null && urlSite!=null) {
                        Log.i("info---",title+" "+urlSite);
                        URL website;
                        HttpURLConnection connection1=null;
                        website=new URL(urlSite);
                        connection1=(HttpURLConnection)website.openConnection();
                        connection1.connect();
                       InputStream in1 = connection1.getInputStream();
                        InputStreamReader reader1=new InputStreamReader(in1);
                        String urlContent="";
                        int datax=reader1.read();
                        while(datax!=-1)
                        {
                            char current1=(char)datax;
                             //urlContent+=current1;
                            datax=reader1.read();
                        }
                       String sql="INSERT INTO articles (title,content) VALUES(?,?)";
                        SQLiteStatement statement=articlesDb.compileStatement(sql);
                        statement.bindString(1,title);
                        statement.bindString(2,urlSite);
                        statement.execute();


                    }

                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateList();

        }
    }
}
