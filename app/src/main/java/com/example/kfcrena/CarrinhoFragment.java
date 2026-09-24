package com.example.kfcrena;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.Locale;

public class CarrinhoFragment extends Fragment {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private TextView tvSubtotal;
    private TextView tvDiscount;
    private TextView tvDiscountLabel;
    private TextView tvTotal;
    private View layoutDiscountRow;
    private EditText etCouponCode;
    private MaterialButton btnApplyCoupon;
    private CartManager cartManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carrinho, container, false);

        cartManager = CartManager.getInstance();

        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvDiscount = view.findViewById(R.id.tvDiscount);
        tvDiscountLabel = view.findViewById(R.id.tvDiscountLabel);
        tvTotal = view.findViewById(R.id.tvTotal);
        layoutDiscountRow = view.findViewById(R.id.layoutDiscountRow);
        etCouponCode = view.findViewById(R.id.etCouponCode);
        btnApplyCoupon = view.findViewById(R.id.btnApplyCoupon);
        rvCart = view.findViewById(R.id.rvCart);

        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));

        setupAdapter();
        setupCouponListener();
        updateTotals();

        Button btnCheckout = view.findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getCartItems().isEmpty()) {
                Toast.makeText(getContext(), "Seu carrinho está vazio!", Toast.LENGTH_SHORT).show();
            } else {
                double finalAmount = cartManager.getFinalPrice();
                String msg = String.format(Locale.getDefault(), "Pedido finalizado com sucesso! Total: R$ %.2f", finalAmount);
                Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();

                cartManager.clearCart();
                etCouponCode.setText("");
                updateTotals();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });

        return view;
    }

    private void setupAdapter() {
        adapter = new CartAdapter(cartManager.getCartItems(), product -> {
            cartManager.removeProduct(product);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            updateTotals();
            Toast.makeText(getContext(), product.getName() + " removido", Toast.LENGTH_SHORT).show();
        });
        rvCart.setAdapter(adapter);
    }

    private void setupCouponListener() {
        btnApplyCoupon.setOnClickListener(v -> {
            if (cartManager.getAppliedCouponCode() != null) {
                // Remover cupom já aplicado
                cartManager.removeCoupon();
                etCouponCode.setText("");
                etCouponCode.setEnabled(true);
                btnApplyCoupon.setText(R.string.btn_apply_coupon);
                Toast.makeText(getContext(), "Cupom removido", Toast.LENGTH_SHORT).show();
                updateTotals();
            } else {
                String inputCode = etCouponCode.getText().toString();
                if (inputCode.trim().isEmpty()) {
                    Toast.makeText(getContext(), "Digite um código de cupom!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cartManager.getCartItems().isEmpty()) {
                    Toast.makeText(getContext(), "Adicione produtos ao carrinho antes de aplicar o cupom!", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = cartManager.applyCoupon(inputCode);
                if (success) {
                    etCouponCode.setEnabled(false);
                    btnApplyCoupon.setText(R.string.btn_remove_coupon);
                    Toast.makeText(getContext(), "Cupom aplicado com sucesso!", Toast.LENGTH_SHORT).show();
                    updateTotals();
                } else {
                    Toast.makeText(getContext(), "Cupom inválido! Tente FRETEKFC, BALDE50 ou KFC10.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Restaurar estado do cupom se já houver um aplicado
        if (cartManager.getAppliedCouponCode() != null) {
            etCouponCode.setText(cartManager.getAppliedCouponCode());
            etCouponCode.setEnabled(false);
            btnApplyCoupon.setText(R.string.btn_remove_coupon);
        }
    }

    private void updateTotals() {
        double subtotal = cartManager.getTotalPrice();
        double discount = cartManager.getDiscountAmount();
        double finalTotal = cartManager.getFinalPrice();

        if (tvSubtotal != null) {
            tvSubtotal.setText(String.format(Locale.getDefault(), "R$ %.2f", subtotal));
        }

        if (discount > 0) {
            if (layoutDiscountRow != null) layoutDiscountRow.setVisibility(View.VISIBLE);
            if (tvDiscount != null) {
                tvDiscount.setText(String.format(Locale.getDefault(), "- R$ %.2f", discount));
            }
            if (tvDiscountLabel != null && cartManager.getAppliedCouponCode() != null) {
                tvDiscountLabel.setText(getString(R.string.lbl_discount_with_code, cartManager.getAppliedCouponCode()));
            }
        } else {
            if (layoutDiscountRow != null) layoutDiscountRow.setVisibility(View.GONE);
        }

        if (tvTotal != null) {
            tvTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", finalTotal));
        }
    }
}