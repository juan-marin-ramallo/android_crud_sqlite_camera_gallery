package com.example.taller6_marin;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText edittext_nombre,edittext_edad;
    private ImageView imageView;
    private Spinner spinner_personas;

    private Bitmap imageBitmap;
    private String imagePath;
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private Persona person;
    private List<Persona> person_list;
    private boolean activarSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edittext_nombre = findViewById(R.id.edittext_nombre);
        edittext_edad = findViewById(R.id.edittext_edad);
        imageView = findViewById(R.id.imageView);
        spinner_personas = findViewById(R.id.spinner_personas);

        spinner_personas.setOnItemSelectedListener(this);

        updatePersonSpinner();

        enableStoragePermission();
        enableCameraPermission();
    }

    private void initializeForm(){
        //Vaciar objetos
        person = null;
        imageBitmap = null;
        imagePath = null;

        //Limpiar controles
        edittext_nombre.setText("");
        edittext_edad.setText("");
        imageView.setImageBitmap(null);
        imageView.setBackgroundResource(android.R.drawable.ic_menu_camera);
    }

    private void updatePersonSpinner(){
        try {
            MySQLiteHelper db = MySQLiteHelper.getInstance(this);
            person_list = db.consultarTodasLasPersonas();

            activarSpinner= false;

            ArrayAdapter spinner_adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, person_list);
            spinner_personas.setAdapter(spinner_adapter);
        }
        catch(Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void enableStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                Intent intentStorage = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intentStorage);
            }
        }
    }

    private void enableCameraPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permiso de camara autorizado", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "Permiso de camara NO autorizado. No se puede tomar foto", Toast.LENGTH_LONG).show();
        }
    }

    public void crearNuevo(View view) {
        initializeForm();
    }

    public void eliminar(View view) {
        if(person != null){
            MySQLiteHelper db = MySQLiteHelper.getInstance(this);

            if(db.eliminarPersona(person))
                Toast.makeText(this, "Persona eliminada con exito", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "No se pudo eliminar la persona", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this, "Por favor seleccionar una persona a eliminar del spinner", Toast.LENGTH_LONG).show();

        updatePersonSpinner();
        initializeForm();
    }

    public void grabar(View view) {
        if (imageBitmap != null)
            saveImageOnDevice();

        MySQLiteHelper db = MySQLiteHelper.getInstance(this);

        //person == null (Estoy creando una nueva persona)
        //person != null (Estoy actualizando una persona existente)
        if(person == null){
            person = new Persona();
            person.setNombre(edittext_nombre.getText().toString());
            person.setEdad(Integer.parseInt(edittext_edad.getText().toString()));
            if(imagePath != null)
                person.setRutaFoto(imagePath);

            if(db.agregarPersona(person))
                Toast.makeText(this, "Persona agregada con exito", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "No se pudo ingresar la persona", Toast.LENGTH_LONG).show();
        }
        else{
            person.setNombre(edittext_nombre.getText().toString());
            person.setEdad(Integer.parseInt(edittext_edad.getText().toString()));
            if(imagePath != null)
                person.setRutaFoto(imagePath);

            if(db.actualizarPersona(person))
                Toast.makeText(this, "Persona actualizada con exito", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "No se pudo actualizar la persona", Toast.LENGTH_LONG).show();
        }

        updatePersonSpinner();
        initializeForm();
    }

    private void saveImageOnDevice(){
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
            String imageFileName = "Taller6_Marin_" + timeStamp + ".jpg";

            File fileImage = new File(Environment.getExternalStorageDirectory(), imageFileName);

            if (fileImage.exists())
                fileImage.delete();

            fileImage.createNewFile();
            imagePath = fileImage.getAbsolutePath();

            //Convertir el bitmap a un arreglo de byte
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG,100, baos);
            byte[] bitmapByteArray = baos.toByteArray();

            //Escribo el flujo de bytes en el archivo
            FileOutputStream fos = new FileOutputStream(fileImage);
            fos.write(bitmapByteArray);
            fos.flush();
            fos.close();
        } catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void tomarFoto(View view) {
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraActivityResultLauncher.launch(intentCamera);
    }

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Bundle extras = result.getData().getExtras();

                        imageBitmap = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(imageBitmap);
                        imageView.setBackground(null);
                    }
                }
            }
    );

    public void mostrarGaleria(View view) {
        Intent intentGallery = new Intent();
        intentGallery.setType("image/*");
        //intentGallery.setAction(Intent.ACTION_GET_CONTENT);
        intentGallery.setAction(Intent.ACTION_PICK);

        galleryActivityResultLauncher.launch(intentGallery);
    }

    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        try {
                            Uri imageUri = result.getData().getData();

                            InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            imageBitmap = BitmapFactory.decodeStream(imageStream);

                            imageView.setImageBitmap(imageBitmap);
                            imageView.setBackground(null);
                        }
                        catch(Exception ex){
                            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
    );

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if(adapterView.getId() == R.id.spinner_personas){
            if(activarSpinner) {
                person = person_list.get(position);
                edittext_nombre.setText(person.getNombre());
                edittext_edad.setText(String.valueOf(person.getEdad()));
                imagePath = person.getRutaFoto();
                if (imagePath == null) {
                    imageView.setBackgroundResource(android.R.drawable.ic_menu_camera);
                    imageView.setImageBitmap(null);
                } else {
                    imageBitmap = BitmapFactory.decodeFile(imagePath);
                    imageView.setBackground(null);
                    imageView.setImageBitmap(imageBitmap);
                }
            }
            activarSpinner = true;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}