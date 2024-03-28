package com.example.gbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context mContext;
    private List<birdModel> mUploads;
    private OnItemClickListener mListener;

    public Adapter(Context context, List<birdModel> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.listitem, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        birdModel uploadCurrent = mUploads.get(position);
        holder.textViewName.setText(uploadCurrent.getBirdnames());
        holder.textLocation.setText(uploadCurrent.getBirdCity());
        holder.textDate.setText(uploadCurrent.getDate());

        // invisible texts
        holder.eCountbird.setVisibility(View.INVISIBLE);
        holder.eNotes.setVisibility(View.INVISIBLE);
        holder.eCity.setVisibility(View.INVISIBLE);
        holder.eCountry.setVisibility(View.INVISIBLE);
        holder.eLatitude.setVisibility(View.INVISIBLE);
        holder.eLongitude.setVisibility(View.INVISIBLE);


        Picasso.get()
                .load(uploadCurrent.getPhotoUrl())
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerInside()
                .into(holder.imageView);

        final int currentPosition = position;
        holder.optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view, currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    private void showPopupMenu(View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        popupMenu.inflate(R.menu.delete_menu);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (mListener != null && menuItem.getItemId() == R.id.menu_delete) {
                    mListener.onDeleteClick(position);
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName, textLocation, textDate, eCountbird, eNotes, eCity, eCountry, eLatitude, eLongitude;
        public ImageView imageView;
        public Button optionButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.textBirdName);
            textLocation = itemView.findViewById(R.id.textLocation);
            textDate = itemView.findViewById(R.id.textDate);
            imageView = itemView.findViewById(R.id.imageBird);
            optionButton = itemView.findViewById(R.id.option);
            eCountbird = itemView.findViewById(R.id.countbird);
            eNotes = itemView.findViewById(R.id.notes);
            eCity = itemView.findViewById(R.id.city);
            eCountry = itemView.findViewById(R.id.country);
            eLatitude = itemView.findViewById(R.id.latitude);
            eLongitude = itemView.findViewById(R.id.longitude);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onWhatEverClick(int position);

        void onDeleteClick(int position);

        void onPositionClick(int position);

        int getPosition();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
