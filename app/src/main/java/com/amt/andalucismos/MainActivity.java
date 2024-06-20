package com.amt.andalucismos;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.amt.andalucismos.models.Palabra;
import com.amt.andalucismos.ui.detallePalabra.DetallePalabraFragment;
import com.amt.andalucismos.ui.favoritos.FavoritosFragment;
import com.amt.andalucismos.ui.historial.HistorialFragment;
import com.amt.andalucismos.ui.home.HomeFragment;
import com.amt.andalucismos.ui.propias.PropiasFragment;
import com.amt.andalucismos.utils.MainViewModel;
import com.amt.andalucismos.utils.Notificaciones;
import com.amt.andalucismos.utils.NotificationReceiver;
import com.amt.andalucismos.utils.Ordenable;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import android.Manifest;
import androidx.navigation.fragment.NavHostFragment;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.amt.andalucismos.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_POST_NOTIFICATIONS = 1;
    private static final String CHANNEL_ID = "palabraDelDiaChannel";
    private static boolean ES_NOTIFICACION_DIARIA = false;
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

        // Verifica si la notificación diaria ya ha sido programada
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isNotificationScheduled = preferences.getBoolean("is_notification_scheduled", false);

        if (!isNotificationScheduled) {
            programarNotificacionDiaria();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_notification_scheduled", true);
            editor.apply();
        }

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

        // Crear canal de notificaciones
        createNotificationChannel();

        programarNotificacionDiaria();

        // Solicitar permisos de notificación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATIONS);
        }

        // Manejar el intent si viene de la notificación
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("palabraId")) {
            String palabraId = intent.getStringExtra("palabraId");
            if (palabraId != null) {
                abrirDetallePalabra(palabraId);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        manejarIntent(intent);
    }

    private void abrirDetallePalabra(String palabraId) {
        // Navegar al DetallePalabraFragment con la palabraId
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container_fragment);
        MainViewModel mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        mainViewModel.getPalabraById(palabraId).observe(this, palabra -> {
            if (palabra != null) {
                Bundle args = new Bundle();
                args.putParcelable("palabra", palabra);

                Fragment fragment = currentFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                DetallePalabraFragment detallePalabraFragment = new DetallePalabraFragment();
                detallePalabraFragment.setArguments(args);

                detallePalabraFragment.setArguments(args);
                NavHostFragment.findNavController(fragment).navigate(R.id.action_nav_home_to_nav_detalle_palabra, args);
            }
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

    private void manejarIntent(Intent intent) {
        if (intent.hasExtra("expresionId")) {
            String expresionId = intent.getStringExtra("expresionId");

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Bundle args = new Bundle();
                args.putString("expresionId", expresionId);
                NavHostFragment.findNavController(getSupportFragmentManager().findFragmentById(R.id.container_fragment))
                        .navigate(R.id.action_nav_home_to_nav_detalle_palabra, args);
            } else {
                Intent loginIntent = new Intent(this, LoginActivity.class);
                loginIntent.putExtra("expresionId", expresionId);
                startActivity(loginIntent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                programarNotificacionDiaria();
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Palabra del Día";
            String description = "Canal para notificaciones de palabra del día";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void programarNotificacionDiaria() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 45);
        calendar.set(Calendar.SECOND, 00);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void manejarOrdenOrdenable(@NonNull MenuItem item, Ordenable fragment) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_alfabetico_asc) {
            fragment.ordenarAZ();
        } else if (itemId == R.id.action_alfabetico_desc) {
            fragment.ordenarZA();
        } else if (itemId == R.id.action_mas_favoritas) {
            fragment.ordenarFavoritas();
        } else if (itemId == R.id.action_mas_recientes) {
            fragment.ordenarMasRecientes();
        } else if (itemId == R.id.action_mas_antiguas) {
            fragment.ordenarMasAntiguas();
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
                public boolean onQueryTextChange(String nuevoTexto) {
                    Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container_fragment);

                    if (currentFragment instanceof NavHostFragment) {
                        Fragment fragment = currentFragment.getChildFragmentManager().getPrimaryNavigationFragment();
                        if (fragment instanceof HomeFragment) {
                            Log.d("MainActivity", "Texto escrito: " + nuevoTexto);
                            ((HomeFragment) fragment).filtrarPalabras(nuevoTexto);
                        }
                        else if (fragment instanceof FavoritosFragment) {
                            ((FavoritosFragment) fragment).filtrarPalabras(nuevoTexto);
                        }
                        else if (fragment instanceof HistorialFragment) {
                            ((HistorialFragment) fragment).filtrarPalabras(nuevoTexto);
                        }
                        else if (fragment instanceof PropiasFragment) {
                            ((PropiasFragment) fragment).filtrarPalabras(nuevoTexto);
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
