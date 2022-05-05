package com.example.taller6_marin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "taller6.sqlite";
    private static final int DB_VERSION = 1;
    private static MySQLiteHelper mySQLiteHelperUniqueInstance;


    public static synchronized MySQLiteHelper getInstance(Context context){
        if(mySQLiteHelperUniqueInstance == null)
            mySQLiteHelperUniqueInstance = new MySQLiteHelper(context);

        return mySQLiteHelperUniqueInstance;
    }

    private MySQLiteHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION );
    }

    //Este metodo es importante y es ejecutado cuando la base de datos es creada por primera vez
    //Si la base de datos ya existe, este metodo YA NO SERA ejecutado
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE persona(id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, edad INTEGER, rutaFoto TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean agregarPersona(Persona persona){
        boolean resultado = false;

        //Escribir codigo para tratar de insertar en la BD y cambiar el resultado a true
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try{
            if(db != null){
                ContentValues valores = new ContentValues();
                valores.put("nombre", persona.getNombre());
                valores.put("edad", persona.getEdad());
                valores.put("rutaFoto", persona.getRutaFoto());
                db.insert("persona",null, valores);
                db.setTransactionSuccessful();
                resultado = true;
            }
        } catch(Exception ex){
            Log.d("MySQLiteHelper", ex.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }

        return resultado;
    }

    public boolean actualizarPersona(Persona persona){
        boolean resultado = false;

        //Escribir codigo para tratar de actualizar en la BD y cambiar el resultado a true
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try  {
            if(db != null){
                ContentValues valores = new ContentValues();
                valores.put("nombre", persona.getNombre());
                valores.put("edad", persona.getEdad());
                valores.put("rutaFoto", persona.getRutaFoto());
                String[] condiciones = new String[] {String.valueOf(persona.getId())};
                db.update("persona", valores, "id=?", condiciones);
                db.setTransactionSuccessful();
                resultado = true;
            }
        } catch(Exception ex){
            Log.d("MySQLiteHelper", ex.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }

        return resultado;
    }

    public boolean eliminarPersona(Persona persona){
        boolean resultado = false;

        //Escribir codigo para tratar de eliminar en la BD y cambiar el resultado a true
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            String[] condiciones = new String[] {String.valueOf(persona.getId())};
            db.delete("persona","id=?", condiciones);
            db.setTransactionSuccessful();
            resultado = true;
        } catch(Exception ex)
        {
            Log.d("MySQLiteHelper", ex.getMessage());
        } finally{
            db.endTransaction();
            db.close();
        }

        return resultado;
    }


    public Persona consultarPersona(int id){
        Persona persona = null;

        //Escribir codigo para tratar de consultar de la BD a una persona por su id
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try{
            String[] columnasMostrar = new String[]{"id","nombre","edad","rutaFoto"};
            String[] condicionesFiltrar = new String[] {String.valueOf(id)};
            cursor = db.query("persona",columnasMostrar,"id=?",condicionesFiltrar,null,null,null);

            if(cursor != null){
                cursor.moveToFirst();

                persona = new Persona();
                persona.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                persona.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                persona.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow("edad")));
                persona.setRutaFoto(cursor.getString(cursor.getColumnIndexOrThrow("rutaFoto")));
            }
        } catch(Exception ex){
            Log.d("MySQLiteHelper", ex.getMessage());
        } finally{
            cursor.close();
            db.close();
        }

        return persona;
    }

    public List<Persona> consultarTodasLasPersonas(){
        List<Persona> listaPersonas = new ArrayList<>();

        //Escribir codigo para tratar de consultar de la BD a todas las personas registradas
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;

        try{
            String[] columnasMostrar = new String[]{"id","nombre","edad","rutaFoto"};
            cursor = db.query("persona",columnasMostrar,null,null,null,null,null);

            if(cursor != null){
                //Veo si hay registros en la BD
                if(cursor.getCount()>0){

                    while(cursor.moveToNext()){
                        Persona persona = new Persona();
                        persona.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                        persona.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
                        persona.setEdad(cursor.getInt(cursor.getColumnIndexOrThrow("edad")));
                        persona.setRutaFoto(cursor.getString(cursor.getColumnIndexOrThrow("rutaFoto")));

                        //Agrego a la persona a la lista
                        listaPersonas.add(persona);
                    }
                }
            }
        }catch(Exception ex){
            Log.d("MySQLiteHelper", ex.getMessage());
        }finally{
            cursor.close();
            db.close();
        }

        return listaPersonas;
    }
}
