package com.example.sambungayat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class LogoutBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_logout, container, false);

        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnLogoutConfirm = view.findViewById(R.id.btnLogoutConfirm);

        btnCancel.setOnClickListener(v -> dismiss());

        btnLogoutConfirm.setOnClickListener(v -> {
            // 1. Clear Local Session
            SharedPreferences sharedPref = requireContext().getSharedPreferences("SambungAyatPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.apply();

            // 2. Sign Out dari Google agar sesi bersih
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
            
            mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                Toast.makeText(getContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show();

                // 3. Kembali ke Login
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                dismiss();
            });
        });

        return view;
    }
}
