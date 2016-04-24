package com.evalwithin.olook;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.evalwithin.olook.Data.DataManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GPSListener, OrientationListener {


    private GPSTracker gpsTracker;
    private OrientationTracker orientationTracker;

    private static String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout1);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        MapUtils.init(getResources());

        DataManager dataManager = DataManager.getInstance();
        dataManager.start();

        gpsTracker = new GPSTracker(getApplicationContext());
        orientationTracker = new OrientationTracker(getApplicationContext());
        orientationTracker.addListener(this);

        Menu menu = navigationView.getMenu();
        MenuItem cameraItem = menu.findItem(R.id.nav_camera);
        cameraItem.setIcon(R.drawable.ic_menu_camera);
        cameraItem.setTitle(R.string.camera);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(menu.size() == 0)
        {
            for(int i = 0; i < 5; i++)
            {
                menu.add(0, i, Menu.NONE, "item " + i).setCheckable(true);
            }
        }

        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        menuItem.setChecked(!menuItem.isChecked());
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout1);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        //mMap.getUiSettings().setScrollGesturesEnabled(false);
        //mMap.getUiSettings().setCompassEnabled(false);

        Location loc = gpsTracker.getLocation();
        LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());

        centerMap(loc, 15);

        MapUtils.setMyLocation(mMap, ll);
        MapUtils.addInterestPoint(googleMap, new LatLng(-30, 140), "Nice area!");

        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            private float lastZoom = 1;
            private LatLng lastPos = new LatLng(0, 0);

            private float cumulativeZoom = 0;

            @Override
            public void onCameraChange(CameraPosition pos) {
                float maxLevel = 18f;
                float minLevel = 15f;

                if (pos.zoom > maxLevel) {
                    // TODO : Mettre le mode camera
                    //googleMap.animateCamera(CameraUpdateFactory.zoomTo(maxLevel), 0, null);
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(maxLevel));
                } else if (pos.zoom < minLevel) {
                    //googleMap.animateCamera(CameraUpdateFactory.zoomTo(minLevel), 0, null);
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(minLevel));
                }

                boolean needToCenter = false;

                if (cumulativeZoom < 0.2) {
                    cumulativeZoom += pos.zoom - lastZoom;
                } else {
                    needToCenter = true;
                    cumulativeZoom = 0;
                }

                if (needToCenter) {
                    centerMap(gpsTracker.getLocation());
                }

                lastZoom = pos.zoom;
            }
        });

        // rotate 90 degrees
        CameraPosition oldPos = googleMap.getCameraPosition();
        CameraPosition pos = CameraPosition.builder(oldPos).bearing(245.0F).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
    }

    @Override
    public void onOrientationChanged(float orientation) {
        //System.out.println(orientation);
    }

    @Override
    public void onAccuracyChanged(int accuracy) {

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent myIntent = new Intent(this, CameraActivity.class);
            this.startActivity(myIntent);
        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout1);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onGPSLocationChanged(Location newLocation) {
        centerMap(newLocation);
        LatLng ll = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
        MapUtils.setMyLocation(mMap, ll);
    }

    private void centerMap(Location location) {
        if(location == null)
            return;

        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
    }

    private void centerMap(Location location, float zoom) {
        if(location == null)
            return;

        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoom));
    }
}
