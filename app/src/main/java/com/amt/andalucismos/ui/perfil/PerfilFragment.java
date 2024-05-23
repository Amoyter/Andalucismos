package com.amt.andalucismos.ui.perfil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amt.andalucismos.LoginActivity;
import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.R;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PerfilFragment extends Fragment {
    private boolean imagenCargada = false;
    private Context c;
    private DatabaseReference databaseReference;
    String storageRuta = "fotoPerfil/*";
    private Uri fotoUri;
    private FirebaseAuth fbAuth;
    private View v;
    private String userId;
    private MainActivity mainActivity;
    private ProgressBar progressBar;
    private EditText txtBiografiaPerfil;
    private ImageButton imgBtnPerfil;
    private TextView txtUsuarioPerfil, txtCorreoPerfil;
    private Button btnGuardarCambios, btnCerrarSesion;
    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        fotoUri = data.getData();
                        imgBtnPerfil.setImageURI(fotoUri); // Ejemplo: Mostrar la imagen seleccionada en el ImageButton
                    }
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_perfil, container, false);
        inicializarComponentes();
        getUserId();
        confTxtBiografiaPerfil();
        obtenerDatos();
        inicializarEventosClick();
        obtenerFoto();
        return v;
    }

    private void inicializarComponentes() {
        c = getContext();
        mainActivity = (MainActivity) getActivity();
        fbAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        txtUsuarioPerfil = v.findViewById(R.id.txtUsuarioPerfil);
        txtCorreoPerfil = v.findViewById(R.id.txtCorreoPerfil);
        txtBiografiaPerfil = v.findViewById(R.id.txtBiografiaPerfil);
        progressBar = v.findViewById(R.id.progressBar);
        imgBtnPerfil = v.findViewById(R.id.imgBtnPerfil);
        btnGuardarCambios = v.findViewById(R.id.btnGuardarCambios);
        btnCerrarSesion = v.findViewById(R.id.btnCerrarSesion);
    }

    private void confTxtBiografiaPerfil() {
        txtBiografiaPerfil.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                txtBiografiaPerfil.setMaxHeight(Integer.MAX_VALUE); // Expandir al seleccionar
            } else {
                txtBiografiaPerfil.setMaxHeight(dpToPx(150)); // Colapsar al deseleccionar
            }
        });

    } // confTxtBiografiaPerfil()

    private void obtenerDatos() {
        DatabaseReference userRef = databaseReference.child("usuarios").child(userId);
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.hayConexion()) {
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String sNombre = dataSnapshot.child("nombre").getValue(String.class);
                        String sEmail = dataSnapshot.child("email").getValue(String.class);
                        String sBiografia = dataSnapshot.child("bio").getValue(String.class);

                        txtUsuarioPerfil.setText(sNombre);
                        txtCorreoPerfil.setText(sEmail);
                        txtBiografiaPerfil.setText(sBiografia);
                    } else {
                        Notificaciones.makeToast(c, "No se encontraron datos del usuario", Toast.LENGTH_SHORT);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Notificaciones.makeToast(c, "Error al obtener los datos del usuario", Toast.LENGTH_SHORT);
                }
            });
        } else {
            Notificaciones.makeToast(c, "No hay conexión a Internet. No se puede cargar el perfil.", Toast.LENGTH_SHORT);
        }
        if(txtBiografiaPerfil.getText().toString().isEmpty()){ txtBiografiaPerfil.setText("Sin biografía"); }
        if(txtUsuarioPerfil.getText().toString().isEmpty()){ txtUsuarioPerfil.setText("Sin datos"); }
        if(txtCorreoPerfil.getText().toString().isEmpty()){ txtCorreoPerfil.setText("Sin datos"); }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void inicializarEventosClick(){
        txtBiografiaPerfil.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int lineas = txtBiografiaPerfil.getLineCount();
                if (lineas < 4) {
                    txtBiografiaPerfil.setLines(lineas);
                } else {
                    txtBiografiaPerfil.setLines(4);
                }
            }
        }); // btnBiografiaFerfil
        imgBtnPerfil.setOnClickListener(view -> elegirFoto()); // imgBtnPerfil
        btnGuardarCambios.setOnClickListener(view -> guardarCambios()); // btnGuardarCambios
        btnCerrarSesion.setOnClickListener(view -> mainActivity.manejarCerrarSesion()); // btnCerrarSesion
    } // inicializarEventosClick()

    private void elegirFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startForResult.launch(intent);
    } // elegirFoto()

    private void guardarCambios() {
        if(fotoUri != null){ subirFoto(); }
        subirBiografia();
        Notificaciones.makeToast(c, "Perfil actualizado", Toast.LENGTH_SHORT);
    } // guardarCambios()

    private void subirBiografia() {
        String nuevaBiografia = txtBiografiaPerfil.getText().toString();
        DatabaseReference bioRef = databaseReference.child("usuarios").child(userId).child("bio");

        bioRef.setValue(nuevaBiografia).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) { Notificaciones.makeToast(c, "Error al actualizar la biografía", Toast.LENGTH_SHORT); }
        });
    } // subirBiografia()

    private void subirFoto() {
        if (fotoUri != null) {
            StorageReference fileReference =  FirebaseStorage.getInstance().getReference(storageRuta).child("fotoPerfil/"+userId+".jpg");

            fileReference.putFile(fotoUri).addOnFailureListener(e -> {
                        Notificaciones.makeToast(c, "Error al subir la imagen", Toast.LENGTH_SHORT);
            });
        } else {
            Notificaciones.makeToast(c, "Error al obtener la imagen", Toast.LENGTH_SHORT);
        }
    } // subirFoto()

    private void obtenerFoto() {
        StorageReference fileReference =  FirebaseStorage.getInstance().getReference(storageRuta).child("fotoPerfil/"+userId+".jpg");
        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
            imagenCargada = true;
            cargarImagenRedondeada(uri.toString());
        }).addOnFailureListener(e -> {
            if(imagenCargada){
                Notificaciones.makeToast(c, "Error al cargar la imagen", Toast.LENGTH_SHORT);
                imgBtnPerfil.setImageResource(R.mipmap.ic_launcher_round);
            }
            progressBar.setVisibility(View.GONE);
        });
    } // obtenerFoto()

    private void getUserId () {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    } // getUserId()

    private void cargarImagenRedondeada(String uri) {
        progressBar.setVisibility(View.VISIBLE);
        Uri uUri = Uri.parse(uri);
        Picasso.get()
                .load(uUri)
                .transform(new Transformation() {
                    @Override
                    public Bitmap transform(Bitmap source) {
                        Bitmap rotatedBitmap;
                        try {
                            rotatedBitmap = rotateImageIfRequired(source, uUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            rotatedBitmap = source;
                        }

                        int size = Math.min(rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
                        int x = (rotatedBitmap.getWidth() - size) / 2;
                        int y = (rotatedBitmap.getHeight() - size) / 2;

                        Bitmap squaredBitmap = Bitmap.createBitmap(rotatedBitmap, x, y, size, size);
                        if (squaredBitmap != rotatedBitmap) {
                            rotatedBitmap.recycle();
                        }

                        Bitmap bitmap = Bitmap.createBitmap(size, size, rotatedBitmap.getConfig());

                        Canvas canvas = new Canvas(bitmap);
                        Paint paint = new Paint();
                        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
                        paint.setShader(shader);
                        paint.setAntiAlias(true);

                        float r = size / 2f;
                        canvas.drawCircle(r, r, r, paint);

                        squaredBitmap.recycle();
                        return bitmap;
                    }

                    @Override
                    public String key() {
                        return "circle";
                    }
                })
                .into(imgBtnPerfil, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {
        ExifInterface exif = new ExifInterface(getActivity().getContentResolver().openInputStream(selectedImage));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}