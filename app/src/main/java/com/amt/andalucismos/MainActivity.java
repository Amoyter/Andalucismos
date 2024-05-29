package com.amt.andalucismos;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.amt.andalucismos.ui.home.HomeFragment;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.Notificaciones;
import com.amt.andalucismos.utils.Ordenable;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth fbAuth;
    private NavController navController;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private DatabaseReference database;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Redirigir a la pantalla de inicio de sesión
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        fbAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMain.toolbar);

        inicializarComponentesUI();
        configurarBarraNavegacion();

        // Inicializa el ViewModel
        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.setUserId(currentUser.getUid());

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            invalidateOptionsMenu();
        });
    }

    private void inicializarComponentesUI() {
        drawer = binding.drawerLayout;
        navigationView = binding.navView;
    }

    private void configurarBarraNavegacion() {
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_perfil, R.id.nav_propias, R.id.nav_historial, R.id.nav_favoritos, R.id.nav_nueva, R.id.nav_ajustes, R.id.nav_feedback, R.id.nav_ayuda)
                .setOpenableLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.container_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_salir) {
                manejarCerrarSesion();
                return true;
            } else {
                drawer.closeDrawer(GravityCompat.START);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onSupportNavigateUp();
            }
        });
    }

    public void manejarCerrarSesion() {
        drawer.closeDrawer(GravityCompat.START);
        Notificaciones.makeDialog(this, null, "¿Quieres cerrar sesión?", "SI", "CANCELAR", "", new Notificaciones.RespuestaDialog() {
            @Override
            public void onPositivo() {
                fbAuth.signOut();
                Notificaciones.makeToast(getApplicationContext(), "Sesión cerrada", Toast.LENGTH_SHORT);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onNegativo() {}

            @Override
            public void onNeutral() {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container_fragment);

        if (currentFragment instanceof NavHostFragment) {
            Fragment fragment = currentFragment.getChildFragmentManager().getPrimaryNavigationFragment();

            if (fragment instanceof Ordenable) {
                manejarOrdenOrdenable(item, (Ordenable) fragment);
            } else if (item.getItemId() == R.id.action_politica_privacidad) {
                NavHostFragment.findNavController(fragment).navigate(R.id.action_nav_perfil_to_privacidadFragment);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void manejarOrdenOrdenable(@NonNull MenuItem item, Ordenable fragment) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_alfabetico_asc) {
            fragment.ordenarAZ();
        } else if (itemId == R.id.action_alfabetico_desc) {
            fragment.ordenarZA();
        } else if (itemId == R.id.action_mas_favoritas) {
            fragment.ordenarFavoritas();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        NavController navController = Navigation.findNavController(this, R.id.container_fragment);
        NavDestination currentDestination = navController.getCurrentDestination();

        if (currentDestination != null && (
                currentDestination.getId() == R.id.nav_home ||
                        currentDestination.getId() == R.id.nav_favoritos ||
                        currentDestination.getId() == R.id.nav_propias ||
                        currentDestination.getId() == R.id.nav_historial)) {

            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            searchView.setQueryHint("Buscar");

            searchView.setIconified(false);
            searchView.requestFocus();

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) { return false; }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container_fragment);

                    if (currentFragment instanceof NavHostFragment) {
                        Fragment fragment = currentFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                        if (fragment instanceof HomeFragment) {
                            ((HomeFragment) fragment).filtrarPalabras(newText);
                        }
                    }
                    return false;
                }
            });

            searchView.setOnCloseListener(() -> {
                invalidateOptionsMenu();
                return false;
            });

            return true;
        } else if (currentDestination != null && currentDestination.getId() == R.id.nav_perfil) {
            getMenuInflater().inflate(R.menu.perfil, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.container_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }

    public boolean hayConexion() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }
}
