package com.example.kfcrena;

import android.Manifest;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;

import java.util.ArrayList;
import java.util.List;

public class LojasFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView textGps;
    private TextView tvLojasStatus;
    private Button btnPegarLocalizacao;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private final ActivityResultLauncher<String[]> localizacaoLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineConcedida = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseConcedida = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (Boolean.TRUE.equals(fineConcedida) || Boolean.TRUE.equals(coarseConcedida)) {
                    solicitarAtualizacaoLocalizacao();
                } else {
                    if (textGps != null) {
                        textGps.setText("Permissão de localização negada");
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lojas, container, false);

        textGps = view.findViewById(R.id.textGps);
        tvLojasStatus = view.findViewById(R.id.tvLojasStatus);
        btnPegarLocalizacao = view.findViewById(R.id.btnPegarLocalizacao);

        initPlacesSdk();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Callback para receber a localização GPS
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    if (textGps != null) {
                        textGps.setText("Lat: " + location.getLatitude() + " | Long: " + location.getLongitude());
                    }

                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f));
                    }

                    fetchNearbyKfcStores(userLatLng);
                }

                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        };

        btnPegarLocalizacao.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                solicitarAtualizacaoLocalizacao();
            } else {
                localizacaoLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    private void initPlacesSdk() {
        try {
            ApplicationInfo appInfo = requireContext().getPackageManager()
                    .getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA);
            String apiKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");

            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("COLOQUE_SUA_API_KEY_AQUI")) {
                if (!Places.isInitialized()) {
                    Places.initialize(requireContext().getApplicationContext(), apiKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            solicitarAtualizacaoLocalizacao();
        } else {
            // Posição inicial até que a permissão seja concedida
            LatLng defaultLatLng = new LatLng(-23.550520, -46.633308);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 12f));
            fetchNearbyKfcStores(defaultLatLng);
        }
    }

    private void solicitarAtualizacaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (textGps != null) {
            textGps.setText("Buscando localização...");
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMaxUpdates(1)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void fetchNearbyKfcStores(LatLng centerLatLng) {
        if (mMap == null) return;

        mMap.clear();

        List<KfcStore> stores = getKfcStoresInRegion(centerLatLng);

        for (KfcStore store : stores) {
            mMap.addMarker(new MarkerOptions()
                    .position(store.getLatLng())
                    .title(store.getName())
                    .snippet(store.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        if (tvLojasStatus != null) {
            tvLojasStatus.setText(getString(R.string.lojas_status_found, stores.size()));
        }
    }

    private List<KfcStore> getKfcStoresInRegion(LatLng center) {
        List<KfcStore> stores = new ArrayList<>();

        stores.add(new KfcStore("KFC - Shopping Central",
                new LatLng(center.latitude + 0.005, center.longitude + 0.008),
                "Praça de Alimentação, Lj 102 - Aberto até 22h"));

        stores.add(new KfcStore("KFC - Drive Thru Express",
                new LatLng(center.latitude - 0.008, center.longitude - 0.004),
                "Av. Principal, 1500 - Drive Thru 24h"));

        stores.add(new KfcStore("KFC - Plaza Mall",
                new LatLng(center.latitude + 0.012, center.longitude - 0.009),
                "Shopping Plaza, Piso L2 - Aberto até 23h"));

        return stores;
    }

    private static class KfcStore {
        private final String name;
        private final LatLng latLng;
        private final String address;

        public KfcStore(String name, LatLng latLng, String address) {
            this.name = name;
            this.latLng = latLng;
            this.address = address;
        }

        public String getName() { return name; }
        public LatLng getLatLng() { return latLng; }
        public String getAddress() { return address; }
    }
}