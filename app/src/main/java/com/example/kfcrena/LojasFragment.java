package com.example.kfcrena;

import android.Manifest;
import android.content.Context;
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
import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class LojasFragment extends Fragment {

    private MapView mapView;
    private TextView textGps;
    private TextView tvLojasStatus;
    private Button btnPegarLocalizacao;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // Coordenadas padrão de São Paulo - Brasil (Av. Paulista)
    private static final LatLng SAO_PAULO_BR = new LatLng(-23.5615, -46.6560);

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
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue("KFCRenaAppMobile/1.0 (Android; dev-app)");

        View view = inflater.inflate(R.layout.fragment_lojas, container, false);

        mapView = view.findViewById(R.id.mapView);
        textGps = view.findViewById(R.id.textGps);
        tvLojasStatus = view.findViewById(R.id.tvLojasStatus);
        btnPegarLocalizacao = view.findViewById(R.id.btnPegarLocalizacao);

        setupMapView();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Callback para receber a localização GPS
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    LatLng userLatLng;

                    // Se a localização do emulador for nos EUA (ex: Mountain View), direcionamos para São Paulo, Brasil
                    if (location.getLatitude() > 0) {
                        userLatLng = SAO_PAULO_BR;
                        if (textGps != null) {
                            textGps.setText("Lat: -23.5615 | Long: -46.6560 (São Paulo - Brasil)");
                        }
                    } else {
                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (textGps != null) {
                            textGps.setText("Lat: " + location.getLatitude() + " | Long: " + location.getLongitude());
                        }
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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            solicitarAtualizacaoLocalizacao();
        } else {
            fetchNearbyKfcStores(SAO_PAULO_BR);
        }

        return view;
    }

    private void setupMapView() {
        if (mapView == null) return;

        XYTileSource openStreetMapSource = new XYTileSource(
                "OSM_Public",
                0, 19, 256, ".png",
                new String[]{
                        "https://a.tile.openstreetmap.fr/osmfr/",
                        "https://b.tile.openstreetmap.fr/osmfr/",
                        "https://c.tile.openstreetmap.fr/osmfr/"
                }
        );

        mapView.setTileSource(openStreetMapSource);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.5);
    }

    private void solicitarAtualizacaoLocalizacao() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (textGps != null) {
            textGps.setText("Buscando localização no Brasil...");
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMaxUpdates(1)
                .build();

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void fetchNearbyKfcStores(LatLng centerLatLng) {
        if (mapView == null) return;

        GeoPoint centerPoint = new GeoPoint(centerLatLng.latitude, centerLatLng.longitude);
        mapView.getController().animateTo(centerPoint);

        mapView.getOverlays().clear();

        // Adicionar marcador da localização do usuário
        Marker userMarker = new Marker(mapView);
        userMarker.setPosition(centerPoint);
        userMarker.setTitle("Sua Localização (Brasil)");
        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(userMarker);

        // Buscar e adicionar os restaurantes do KFC no Brasil
        List<KfcStore> stores = getKfcStoresInRegion(centerLatLng);

        for (KfcStore store : stores) {
            GeoPoint storePoint = new GeoPoint(store.getLatLng().latitude, store.getLatLng().longitude);
            Marker storeMarker = new Marker(mapView);
            storeMarker.setPosition(storePoint);
            storeMarker.setTitle(store.getName());
            storeMarker.setSnippet(store.getAddress());
            storeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(storeMarker);
        }

        mapView.invalidate();

        if (tvLojasStatus != null) {
            tvLojasStatus.setText(getString(R.string.lojas_status_found, stores.size()));
        }
    }

    private List<KfcStore> getKfcStoresInRegion(LatLng center) {
        List<KfcStore> stores = new ArrayList<>();

        // Unidades reais do KFC em São Paulo - Brasil
        stores.add(new KfcStore("KFC - Shopping Cidade São Paulo",
                new LatLng(-23.5641, -46.6524),
                "Av. Paulista, 1230 - Piso 3, Bela Vista, São Paulo - SP"));

        stores.add(new KfcStore("KFC - Shopping Ibirapuera",
                new LatLng(-23.6105, -46.6662),
                "Av. Ibirapuera, 3103 - Moema, São Paulo - SP"));

        stores.add(new KfcStore("KFC - Shopping Eldorado",
                new LatLng(-23.5732, -46.6955),
                "Av. Rebouças, 3970 - Pinheiros, São Paulo - SP"));

        stores.add(new KfcStore("KFC - Metrô Tatuapé",
                new LatLng(-23.5398, -46.5768),
                "R. Domingo Agostim, 91 - Tatuapé, São Paulo - SP"));

        stores.add(new KfcStore("KFC - Shopping Anália Franco",
                new LatLng(-23.5620, -46.5600),
                "Av. Reg. Feijó, 1739 - Tatuapé, São Paulo - SP"));

        return stores;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
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