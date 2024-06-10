package com.amt.andalucismos.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amt.andalucismos.MainActivity;
import com.amt.andalucismos.models.Comentario;
import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.models.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainViewModel extends ViewModel {
    private LifecycleOwner lifecycleOwner;
    private final MutableLiveData<List<Palabra>> palabras = new MutableLiveData<>();
    private final MutableLiveData<List<Comentario>> comentarios = new MutableLiveData<>();
    private final MutableLiveData<String> userId = new MutableLiveData<>();
    private final MutableLiveData<Usuario> usuario = new MutableLiveData<>();
    private final MutableLiveData<Palabra> palabraDelDia = new MutableLiveData<>();

    public LiveData<List<Palabra>> getPalabras() {
        return palabras;
    }

    public LiveData<List<Comentario>> getComentarios() { return comentarios; }

    public LiveData<String> getUserId() {
        return userId;
    }

    public LiveData<Usuario> getUsuario() {
        return usuario;
    }

    public LiveData<Palabra> getPalabraDelDia() { return palabraDelDia; }
    public List<String> getHistorial() { return usuario.getValue().getHistorial(); }

    public LiveData<Palabra> getPalabraById(String palabraId) {
        MutableLiveData<Palabra> palabraLiveData = new MutableLiveData<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("contribuciones").child(palabraId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Palabra palabra = snapshot.getValue(Palabra.class);
                palabraLiveData.setValue(palabra);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error al obtener la palabra por ID", error.toException());
            }
        });
        return palabraLiveData;
    }

    public void setUserId(String id) {
        userId.setValue(id);
        loadUsuario(id);
    }


    public void setPalabras(List<Palabra> listaPalabras) {
        palabras.setValue(listaPalabras);
    }

    public void setComentarios(List<Comentario> listaComentarios) { comentarios.setValue(listaComentarios); }

    public void setHistorial(List<String> historial){usuario.getValue().setHistorial(historial);}

    private void loadUsuario(String id) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("usuarios").child(id);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Usuario user = snapshot.getValue(Usuario.class);
                    if (user.getFavoritas() == null) {
                        user.setFavoritas(new ArrayList<>());
                        userRef.child("favoritas").setValue(user.getFavoritas()); // Inicializar la lista de favoritas en la base de datos
                    }
                    if (user.getHistorial() == null) {
                        user.setHistorial(new ArrayList<>());
                        userRef.child("historial").setValue(user.getHistorial()); // Inicializar la lista de historial en la base de datos
                    }
                    usuario.setValue(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar error
            }
        });
    }

    public void loadPalabras() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("contribuciones");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Palabra> palabras = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Palabra palabra = snapshot.getValue(Palabra.class);
                    palabras.add(palabra);
                }
                setPalabras(palabras);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error al obtener los datos", databaseError.toException());
            }
        });
    }

    public void loadComentarios() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("comentarios");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comentario> comentarios = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comentario comentario = snapshot.getValue(Comentario.class);
                    if (!comentario.getRevisado()) {
                        comentarios.add(comentario);
                    }
                }
                Log.d("MainViewModel", "Comentarios: " + comentarios.size());
                setComentarios(comentarios);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error al obtener los datos", databaseError.toException());
            }
        });
    }

    public void actualizarComentario(Comentario comentario, boolean revisado) {
        DatabaseReference comentarioRef = FirebaseDatabase.getInstance().getReference("comentarios").child(comentario.getComentarioId());

        comentarioRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Comentario c = mutableData.getValue(Comentario.class);

                if (c == null) { return Transaction.success(mutableData); }
                if (!revisado) { c.setRevisado(true); }

                mutableData.setValue(c);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError == null && committed) {
                    Log.d("MainViewModel", "Comentario actualizado correctamente.");
                } else {
                    Log.e("MainViewModel", "Error al actualizar palabra: " + databaseError.getMessage());
                }
            }
        });
    }

    public void actualizarFavorito(Palabra palabra, boolean agregar) {
        String userId = usuario.getValue().getId();
        DatabaseReference palabraRef = FirebaseDatabase.getInstance().getReference("contribuciones").child(palabra.getExpresionId());
        DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId).child("favoritas");

        palabraRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Palabra p = mutableData.getValue(Palabra.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (agregar) {
                    p.setNumFavoritas(p.getNumFavoritas() + 1);
                } else {
                    p.setNumFavoritas(p.getNumFavoritas() - 1);
                }

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError == null && committed) {
                    usuarioRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            List<String> favoritas = mutableData.getValue(new GenericTypeIndicator<List<String>>() {});
                            if (favoritas == null) {
                                favoritas = new ArrayList<>();
                            }

                            if (agregar) {
                                if (!favoritas.contains(palabra.getExpresionId())) {
                                    favoritas.add(palabra.getExpresionId());
                                }
                            } else {
                                favoritas.remove(palabra.getExpresionId());
                            }

                            mutableData.setValue(favoritas);
                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                            if (databaseError != null) {
                                Log.e("MainViewModel", "Error al actualizar favoritos: " + databaseError.getMessage());
                            } else {
                                // Actualiza el LiveData del usuario y de las palabras para que las vistas se sincronicen
                                usuario.postValue(usuario.getValue());
                                loadPalabras();
                            }
                        }
                    });
                } else {
                    Log.e("MainViewModel", "Error al actualizar palabra: " + databaseError.getMessage());
                }
            }
        });
    }

    public void actualizarHistorial(Palabra palabra) {
        String userId = usuario.getValue().getId();
        DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId).child("historial");

        usuarioRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                List<String> historial = mutableData.getValue(new GenericTypeIndicator<List<String>>() {});
                if (historial == null) {
                    historial = new ArrayList<>();
                }

                if (historial.contains(palabra.getExpresionId())) {
                    historial.remove(palabra.getExpresionId());
                }

                if (historial.size() >= 50) {
                    historial.remove(0);
                }

                historial.add(palabra.getExpresionId());

                mutableData.setValue(historial);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("MainViewModel", "Error al actualizar historial: " + databaseError.getMessage());
                }
            }
        });
    }

    public void eliminarHistorial() {
        String userId = usuario.getValue().getId();
        DatabaseReference usuarioRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId).child("historial");

        usuario.removeObservers(lifecycleOwner);

        usuarioRef.setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("MainViewModel", "Historial eliminado correctamente.");

                // Limpiar el historial en el LiveData
                Usuario currentUser = usuario.getValue();
                if (currentUser != null) {
                    currentUser.setHistorial(new ArrayList<>());
                    usuario.postValue(currentUser);
                }
            } else {
                Log.e("MainViewModel", "Error al eliminar historial: " + task.getException().getMessage());
            }
        });
    }

    public void loadPalabraDelDia() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("contribuciones");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Palabra> palabras = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Palabra palabra = snapshot.getValue(Palabra.class);
                    palabras.add(palabra);
                }
                if (!palabras.isEmpty()) {
                    int randomIndex = new Random().nextInt(palabras.size());
                    Palabra palabraDelDia = palabras.get(randomIndex);
                    MainViewModel.this.palabraDelDia.setValue(palabraDelDia);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainViewModel", "Error al seleccionar la palabra del día aleatoriamente", databaseError.toException());
            }
        });
    }
}

