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

public class CuponsFragment extends Fragment {
    
    private RecyclerView rvCupons;
    private KfcAdapter adapter;
    private List<KfcItem> couponList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cupons, container, false);

        // Header agora é global

        rvCupons = view.findViewById(R.id.rvCupons);
        rvCupons.setLayoutManager(new LinearLayoutManager(getContext()));

        loadCupons();

        adapter = new KfcAdapter(couponList, item -> {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("item", item);
            startActivity(intent);
        });
        rvCupons.setAdapter(adapter);

        return view;
    }

    private void loadCupons() {
        couponList = new ArrayList<>();
        couponList.add(new KfcItem(getString(R.string.coupon_free_shipping), getString(R.string.coupon_free_shipping_desc), R.drawable.cupom1kfc, "FRETEKFC"));
        couponList.add(new KfcItem(getString(R.string.coupon_2_buckets), getString(R.string.coupon_2_buckets_desc), R.drawable.frangokfc, "BALDE50"));
    }
}