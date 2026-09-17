package com.example.kfcrena;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CardapioFragment extends Fragment {

    private RecyclerView rvProducts;
    private KfcAdapter adapter;
    private List<KfcItem> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cardapio, container, false);

        // O cabeçalho agora é global na MainActivity

        setupRecyclerView(view);
        return view;
    }

    private void setupRecyclerView(View view) {
        rvProducts = view.findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        productList = new ArrayList<>();
        productList.add(new KfcItem(getString(R.string.item_balde_tradicional), 89.90, R.drawable.frangokfc, getString(R.string.desc_balde_tradicional)));
        productList.add(new KfcItem(getString(R.string.item_kentucky_sandwich), 22.90, R.drawable.ofertakfc, getString(R.string.desc_kentucky_sandwich)));
        productList.add(new KfcItem(getString(R.string.item_batata_grande), 12.90, R.drawable.frangokfc, getString(R.string.desc_batata_grande)));

        adapter = new KfcAdapter(productList, item -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("item", item);
            startActivity(intent);
        });

        rvProducts.setAdapter(adapter);
    }
}