package com.amt.andalucismos;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.amt.andalucismos.utils.Notificaciones;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.amt.andalucismos.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity{

    // Declaración de variables de la clase
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth fbAuth;
    private NavController navController;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    /*TODO:
        MainActivity
    *   -----------------------------------------------------
    *   1- Configurar el ViewBinding para referenciar las vistas fácilmente
    *   2- Establecer la barra de herramientas como la ActionBar
    *   3- Definir la configuración de AppBar con los IDs de los destinos de menú como los destinos de nivel superior.
    *   4- Configurar el NavController para la navegación en la aplicación
    *   5- Establecer el listener para los elementos del NavigationView
    *       5.1- Obtener el ID del elemento del menú seleccionado
    *       5.2- Si el elemento seleccionado es "Cerrar sesión", realizar el cierre de sesión
    *       5.3- Si se selecciona cualquier otro elemento, delegar la acción al NavController
    *   6- Inflar el menú; esto agrega items a la barra de acciones si ésta está presente
    *   7- Configurar la navegación hacia arriba (up navigation) para el NavController
    */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fbAuth = FirebaseAuth.getInstance();

        // 1- Configurar el ViewBinding para referenciar las vistas fácilmente
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2- Establecer la barra de herramientas como la ActionBar
        setSupportActionBar(binding.appBarMain.toolbar);
        drawer = binding.drawerLayout;
        navigationView = binding.navView;

        // 3- Definir la configuración de AppBar con los IDs de los destinos de menú como los destinos de nivel superior.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_perfil, R.id.nav_historial, R.id.nav_favoritos, R.id.nav_nueva, R.id.nav_ajustes, R.id.nav_feedback, R.id.nav_ayuda)
                .setOpenableLayout(drawer)
                .build();

        // 4- Configurar el NavController para la navegación en la aplicación
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // 5- Establecer el listener para los elementos del NavigationView
        navigationView.setNavigationItemSelectedListener(item -> {
            // 5.1- Obtener el ID del elemento del menú seleccionado
            int id = item.getItemId();
            // 5.2- Si el elemento seleccionado es "Cerrar sesión", realizar el cierre de sesión
            if(id == R.id.nav_salir){
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
                return true;
            } else {
                // 5.3- Si se selecciona cualquier otro elemento, delegar la acción al NavController
                drawer.closeDrawer(GravityCompat.START);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onSupportNavigateUp();
            }
        });
    } // onCreate()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 6- Inflar el menú; esto agrega items a la barra de acciones si ésta está presente
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    } // onCreateOptionsMenu()

    @Override
    public boolean onSupportNavigateUp() {
        // 7- Configurar la navegación hacia arriba (up navigation) para el NavController
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration) || super.onSupportNavigateUp();
    } // onSupportNavigateUp()
}