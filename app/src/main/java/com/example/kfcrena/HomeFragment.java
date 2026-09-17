package com.example.kfcrena;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // O cabeçalho agora é global e gerenciado pela MainActivity

        // Setup Main Offer Click
        view.findViewById(R.id.cardMainOffer).setOnClickListener(v -> {
            KfcItem offerItem = new KfcItem(
                    getString(R.string.banner_title),
                    39.90,
                    R.drawable.ofertakfc,
                    getString(R.string.banner_subtitle)
            );
            
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra("item", offerItem);
            startActivity(intent);
        });

        // Setup quick actions
        view.findViewById(R.id.cardGoToMenu).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_cardapio);
            }
        });

        view.findViewById(R.id.cardGoToCoupons).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedTab(R.id.nav_cupons);
            }
        });

        return view;
    }
}