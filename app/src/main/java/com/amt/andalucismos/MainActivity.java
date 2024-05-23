package com.amt.andalucismos;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.amt.andalucismos.ui.favoritos.FavoritosFragment;
import com.amt.andalucismos.ui.historial.HistorialFragment;
import com.amt.andalucismos.ui.home.HomeFragment;
import com.amt.andalucismos.utils.Notificaciones;
import com.amt.andalucismos.utils.Ordenable;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.amt.andalucismos.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity{

    // Declaración de variables de la clase
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth fbAuth;
    private NavController navController;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inicializar Firebase Auth y DatabaseReference
        fbAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        // Configurar el ViewBinding para referenciar las vistas fácilmente
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Establecer la barra de herramientas como la ActionBar
        setSupportActionBar(binding.appBarMain.toolbar);

        // Inicializar elementos de la interfaz de usuario
        inicializarComponentesUI();

        // Configurar la barra de navegación
        configurarBarraNavegacion();

        // Listener para los cambios de destino. Fuerza la recreación del menú cada vez que el destino cambie
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {invalidateOptionsMenu();});
    } // onCreate()

    // Método para inicializar los elementos de la interfaz de usuario
    private void inicializarComponentesUI() {
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
    } // inicializarComponentesUI()

    // Métodos para configurar la barra de navegación
    private void configurarBarraNavegacion() {
        // Definir la configuración de AppBar con los IDs de los destinos de menú como los destinos de nivel superior.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_perfil, R.id.nav_historial, R.id.nav_favoritos, R.id.nav_nueva, R.id.nav_ajustes, R.id.nav_feedback, R.id.nav_ayuda)
                .setOpenableLayout(drawer)
                .build();

        // Configurar el NavController para la navegación en la aplicación
        navController = Navigation.findNavController(this, R.id.container_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Establecer el listener para los elementos del NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_salir) {
                manejarCerrarSesion();
                return true;
            } else {
                drawer.closeDrawer(GravityCompat.START);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onSupportNavigateUp();
            }
        });
    } // configurarBarraNavegacion()

    // Método para manejar el cierre de sesión
    public void manejarCerrarSesion() {
        drawer.closeDrawer(GravityCompat.START); // Cierra el drawer de navegación
        Notificaciones.makeDialog(this, null, "¿Quieres cerrar sesión?", "SI", "CANCELAR", "", new Notificaciones.RespuestaDialog() {
            @Override
            public void onPositivo() {
                fbAuth.signOut(); // Cerrar la sesión de Firebase Auth
                Notificaciones.makeToast(getApplicationContext(), "Sesión cerrada", Toast.LENGTH_SHORT);
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class)); // Nos devuelve a la LoginActivity
            }

            @Override
            public void onNegativo() {}

            @Override
            public void onNeutral() {}
        });
    } // manejarCerrarSesion()

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container_fragment);

        if (currentFragment instanceof NavHostFragment) {
            Fragment fragment = currentFragment.getChildFragmentManager().getPrimaryNavigationFragment();

            if (fragment instanceof Ordenable) {
                manejarOrden(item, (Ordenable) fragment);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void manejarOrden(@NonNull MenuItem item, Ordenable fragment) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_alfabetico_asc) {
            fragment.ordenarAZ();
        } else if (itemId == R.id.action_alfabetico_desc) {
            fragment.ordenarZA();
        }
        // Agrega otros criterios de ordenación si es necesario
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        NavController navController = Navigation.findNavController(this, R.id.container_fragment);
        NavDestination currentDestination = navController.getCurrentDestination();

        if (currentDestination != null &&
                (currentDestination.getId() == R.id.nav_home ||
                        currentDestination.getId() == R.id.nav_favoritos ||
                        currentDestination.getId() == R.id.nav_historial)) {

            getMenuInflater().inflate(R.menu.main, menu);

            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Buscar");

            // Para que aparezca directamente seleccionado
            searchView.setIconified(false);
            searchView.requestFocus();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.d("MainActivity", "onQueryTextChange called with: " + newText);
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container_fragment);

                    if (currentFragment instanceof NavHostFragment) {
                        Fragment fragment = ((NavHostFragment) currentFragment).getChildFragmentManager().getPrimaryNavigationFragment();
                        Log.d("MainActivity", "Current fragment: " + fragment);
                        if (fragment instanceof HomeFragment) {
                            Log.d("MainActivity", "Entra en si es el fragment HomeFragment");
                            ((HomeFragment) fragment).filtrarPalabras(newText);
                        }
                    }

                    return false;
                }
            });

            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    Log.d("MainActivity", "SearchView closed");
                    invalidateOptionsMenu(); // Esto forzará la recreación del menú
                    return false;
                }
            });

            return true;
        }

        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Configurar la navegación hacia arriba (up navigation) para el NavController
        navController = Navigation.findNavController(this, R.id.container_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    } // onSupportNavigateUp()

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu(); // Esto forzará la recreación del menú cada vez que se vuelva a la actividad
    } // onResume()

    // Método para obtener datos desde la base de datos
    public void obtenerDatos(String path, ValueEventListener listener) {
        database.child(path).addListenerForSingleValueEvent(listener);
    } // obtenerDatos()

    // Método para escribir datos en la base de datos
    public void escribirDatos(String path, Object value, DatabaseReference.CompletionListener listener) {
        database.child(path).setValue(value, listener);
    } // escribirDatos()

    // Método para comprobar si hay conexión con internet
    public boolean hayConexion(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    } // hayConexión()
} // MainActivity