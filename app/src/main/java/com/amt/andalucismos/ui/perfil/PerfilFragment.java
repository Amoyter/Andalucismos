package com.amt.andalucismos.ui.perfil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.R;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.Notificaciones;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;


public class PerfilFragment extends Fragment {
    public boolean imagenCargada = false;
    private Context c;
    private MainViewModel mainViewModel;
    private Uri fotoUri;
    private View v;
    private ProgressBar progressBar;
    private EditText txtBiografiaPerfil;
    private ImageButton imgBtnPerfil;
    private TextView txtUsuarioPerfil, txtCorreoPerfil;
    private Button btnGuardarCambios, btnCerrarSesion;
    private final ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    fotoUri = result.getData().getData();
                    cargarImagenRedondeada(fotoUri.toString());
                    imgBtnPerfil.setImageURI(fotoUri);
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_perfil, container, false);
        inicializarComponentes();
        observarUsuario();
        inicializarEventosClick();
        obtenerFoto();
        return v;
    }

    private void inicializarComponentes() {
        c = getContext();
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        txtUsuarioPerfil = v.findViewById(R.id.txtUsuarioPerfil);
        txtCorreoPerfil = v.findViewById(R.id.txtCorreoPerfil);
        txtBiografiaPerfil = v.findViewById(R.id.txtBiografiaPerfil);
        progressBar = v.findViewById(R.id.progressBar);
        imgBtnPerfil = v.findViewById(R.id.imgBtnPerfil);
        btnGuardarCambios = v.findViewById(R.id.btnGuardarCambios);
        btnCerrarSesion = v.findViewById(R.id.btnCerrarSesion);
    }

    private void observarUsuario() {
        mainViewModel.getUsuario().observe(getViewLifecycleOwner(), usuario -> {
            if (usuario != null) {
                txtUsuarioPerfil.setText(usuario.getNombre());
                txtCorreoPerfil.setText(usuario.getEmail());
                txtBiografiaPerfil.setText(usuario.getBio());
                txtBiografiaPerfil.setVisibility(View.VISIBLE);
                txtBiografiaPerfil.setEnabled(true);
                txtBiografiaPerfil.setTextColor(Color.BLACK);

                // Añadir el texto en un runnable para forzar el redibujado después de la asignación
                txtBiografiaPerfil.post(() -> txtBiografiaPerfil.setText(usuario.getBio()));
            } else {
                Log.e("PerfilFragment", "Usuario no encontrado");
            }
        });
    }

    private void inicializarEventosClick() {
        txtBiografiaPerfil.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                txtBiografiaPerfil.setLines(Math.min(txtBiografiaPerfil.getLineCount(), 4));
            }
        });
        imgBtnPerfil.setOnClickListener(view -> elegirFoto());
        btnGuardarCambios.setOnClickListener(view -> guardarCambios());
        btnCerrarSesion.setOnClickListener(view -> ((MainActivity) getActivity()).manejarCerrarSesion());
    }

    private void elegirFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startForResult.launch(intent);
    }

    private void guardarCambios() {
        if (fotoUri != null) { subirFoto(); }
        subirBiografia();
        Notificaciones.makeToast(c, "Perfil actualizado", Toast.LENGTH_SHORT);
    }

    private void subirBiografia() {
        String nuevaBiografia = txtBiografiaPerfil.getText().toString();
        String userId = mainViewModel.getUserId().getValue();
        if (userId != null) {
            DatabaseReference bioRef = FirebaseDatabase.getInstance().getReference("usuarios")
                    .child(userId).child("bio");

            bioRef.setValue(nuevaBiografia).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Notificaciones.makeToast(c, "Error al actualizar la biografía", Toast.LENGTH_SHORT);
                }
            });
        } else {
            Notificaciones.makeToast(c, "Error al obtener el ID del usuario", Toast.LENGTH_SHORT);
        }
    }

    private void subirFoto() {
        String userId = mainViewModel.getUserId().getValue();
        if (fotoUri != null && userId != null) {
            StorageReference fileReference = FirebaseStorage.getInstance().getReference("fotoPerfil")
                    .child(userId + ".jpg");

            fileReference.putFile(fotoUri).addOnFailureListener(e -> Notificaciones.makeToast(c, "Error al subir la imagen", Toast.LENGTH_SHORT));
        } else {
            Notificaciones.makeToast(c, "Error al obtener la imagen o ID del usuario", Toast.LENGTH_SHORT);
        }
    }

    private void obtenerFoto() {
        String userId = mainViewModel.getUserId().getValue();
        if (userId != null) {
            StorageReference fileReference = FirebaseStorage.getInstance().getReference("fotoPerfil")
                    .child(userId + ".jpg");
            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                imagenCargada = true;
                cargarImagenRedondeada(uri.toString());
            }).addOnFailureListener(e -> {
                Log.e("PerfilFragment", "Error al cargar la imagen: " + e.getMessage());
                Notificaciones.makeToast(c, "Imagen de perfil no encontrada, usando imagen predeterminada", Toast.LENGTH_SHORT);
                imgBtnPerfil.setImageResource(R.mipmap.ic_launcher_round);
                progressBar.setVisibility(View.GONE);
            });
        } else {
            Notificaciones.makeToast(c, "Error al obtener el ID del usuario", Toast.LENGTH_SHORT);
            imgBtnPerfil.setImageResource(R.mipmap.ic_launcher_round);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void cargarImagenRedondeada(String uri) {
        progressBar.setVisibility(View.VISIBLE);
        Uri uUri = Uri.parse(uri);
        Picasso.get()
                .load(uUri)
                .transform(new CircleTransformation())
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

    private class CircleTransformation implements Transformation {
        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap rotatedBitmap;
            try {
                if (fotoUri != null) {
                    rotatedBitmap = rotateImageIfRequired(source, fotoUri);
                } else {
                    rotatedBitmap = source;
                }
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

