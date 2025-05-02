package com.fast.mentor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {

    private Context context;
    private List<Certificate> certificateList;

    public CertificateAdapter(Context context, List<Certificate> certificateList) {
        this.context = context;
        this.certificateList = certificateList;
    }

    @NonNull
    @Override
    public CertificateAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_certificate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateAdapter.ViewHolder holder, int position) {
        Certificate certificate = certificateList.get(position);
        holder.certImage.setImageResource(certificate.getImageResId());
        holder.courseName.setText(certificate.getCourseName());
        holder.provider.setText(certificate.getProvider());
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView certImage;
        TextView courseName, provider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            certImage = itemView.findViewById(R.id.certImage);
            courseName = itemView.findViewById(R.id.courseName);
            provider = itemView.findViewById(R.id.provider);
        }
    }
}
