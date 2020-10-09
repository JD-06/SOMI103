package com.somi.cheems;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.txusballesteros.bubbles.BubbleLayout;
import com.txusballesteros.bubbles.BubblesManager;
import com.txusballesteros.bubbles.OnInitializedCallback;


public class menu extends Fragment {
    private KarenActions karenActions;
    GetFloatingIconClick receiver;

    public menu() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        CardView btnUbicacion = view.findViewById(R.id.btnUbicacion);
        CardView btnHora = view.findViewById(R.id.btnHora);
        CardView btnIdentificador = view.findViewById(R.id.btnIdentificador);
        karenActions  = new KarenActions();
        karenActions.setContext(getContext());

        btnHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TTS.speak(karenActions.hora());
                Intent startIntent = new Intent(getContext(), FloatWidgetService.class);
                getActivity().startService(startIntent);
            }
        });
        btnUbicacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                karenActions.Ubicacion();
            }
        });
        btnIdentificador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                karenActions.intentGlobal(getContext(),IdentificadorImagenes.class);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        receiver = new GetFloatingIconClick();

    }

    private class GetFloatingIconClick extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent selfIntent = new Intent(getActivity(), MainActivity.class);
            selfIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(selfIntent);
        }
    }

}