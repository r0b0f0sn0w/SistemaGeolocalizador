package com.r0_f0.SistemaGeolocalizador;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.r0_f0.SistemaGeolocalizador.Entidades.UsuarioPortador;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorPortadores extends RecyclerView.Adapter<AdaptadorPortadores.usuariosHolder>{

    List<UsuarioPortador> listaUsuarios;

    public AdaptadorPortadores(List<UsuarioPortador> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @Override
    public AdaptadorPortadores.usuariosHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(parent.getContext()).inflate(R.layout.usuarios_portadores,parent,false);
        RecyclerView.LayoutParams layoutParams= new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        v.setLayoutParams(layoutParams);
        return new usuariosHolder(v);
    }

    @Override
    public void onBindViewHolder(AdaptadorPortadores.usuariosHolder holder, int position) {
        holder.lblNombrePortador.setText(listaUsuarios.get(position).getNombre().toString());
        holder.lblApellidoPortador.setText(listaUsuarios.get(position).getAppPat().toString());
        holder.lblIdPortador.setText(listaUsuarios.get(position).getIdPortador().toString());
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }
    public class usuariosHolder extends RecyclerView.ViewHolder{
        TextView lblNombrePortador,lblApellidoPortador,lblIdPortador;
        public usuariosHolder(View itemView) {
            super(itemView);
            lblNombrePortador=itemView.findViewById(R.id.lblNombrePortador);
            lblApellidoPortador=itemView.findViewById(R.id.lblApellidoPortador);
            lblIdPortador=itemView.findViewById(R.id.lblIdPortador);
        }
    }
}
