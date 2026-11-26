package com.rather.ecosort.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rather.ecosort.LessonActivity;
import com.rather.ecosort.R;
import com.rather.ecosort.models.Lesson;

import java.util.ArrayList;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private final RecyclerViewInterface recyclerViewInterface;

    private Context context;
    private ArrayList<Lesson> lessons_list;

    public LessonAdapter(Context context, ArrayList<Lesson> lessons,
                         RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.lessons_list = lessons;
        this.recyclerViewInterface = recyclerViewInterface;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.lesson_list_item, parent, false);
        return new LessonViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessons_list.get(position);
        holder.lesson_preview_image_view.setImageResource(lesson.getPreview());
        holder.lesson_title_text_view.setText(lesson.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, LessonActivity.class);

                intent.putExtra(LessonActivity.EXTRA_IMAGE, lesson.getMain_image());
                intent.putExtra(LessonActivity.EXTRA_TEXT, lesson.getText());

                context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) context).toBundle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessons_list.size();
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder {

        private ImageView lesson_preview_image_view;
        private ImageView lesson_content_image_view;
        private TextView lesson_title_text_view;
        private TextView lesson_content_text_view;

        public LessonViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);

            lesson_preview_image_view = itemView.findViewById(R.id.lesson_preview_image_view);
            lesson_content_image_view = itemView.findViewById(R.id.lesson_content_image_view);
            lesson_title_text_view = itemView.findViewById(R.id.lesson_title_text_view);
            lesson_content_text_view = itemView.findViewById(R.id.lesson_content_text_view);

        }
    }
}
