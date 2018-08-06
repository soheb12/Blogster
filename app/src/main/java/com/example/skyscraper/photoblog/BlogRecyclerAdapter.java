package com.example.skyscraper.photoblog;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder>{

    public List<BlogPost> blogList;
    public List<User> userList;
    public Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    public BlogRecyclerAdapter(List<BlogPost> blogList,List<User> userList)
    {
        this.blogList = blogList;
        this.userList = userList;
    }




    @NonNull
    @Override
    public BlogRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context = parent.getContext();

        firebaseFirestore  = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlogRecyclerAdapter.ViewHolder holder, final int position) {

        final String blogPostId = blogList.get(position).blogPostId;



        mAuth = FirebaseAuth.getInstance();

        final String currentUserId = mAuth.getCurrentUser().getUid();

        String blogUserId = blogList.get(position).user_id;

        if(blogUserId.equals(currentUserId))
        {
            holder.deletePost.setEnabled(true);
            holder.deletePost.setVisibility(View.VISIBLE);
        }

        holder.deletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        blogList.remove(position);
                    }
                });
            }
        });


        String descData = blogList.get(position).getDesc();
        holder.setDescText(descData);

        try {
            long millisecond = blogList.get(position).getTimestamp().getTime();
            String dateString = DateFormat.format("dd/MM/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {
            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //setting image uri
        final String image_url = blogList.get(position).getImage_uri();
        String thumbUri = blogList.get(position).getThumb_uri();
        holder.setBlogImage(image_url, thumbUri);

        //setting user data
        String username = userList.get(position).name;
        String imageurl = userList.get(position).image;
        holder.setUserData(username,imageurl);

        //count likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if(!documentSnapshots.isEmpty())
                {
                        holder.updateLikesCount(documentSnapshots.size());
                }else
                {
                        holder.updateLikesCount(0);
                }
            }
        });

        //count comments
        firebaseFirestore.collection("Posts/" + blogPostId + "/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if(!documentSnapshots.isEmpty())
                {
                    holder.updateCommentsCount(documentSnapshots.size());
                }else
                {
                    holder.updateCommentsCount(0);
                }

            }
        });



        //user liked or not
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if(documentSnapshot.exists())
                {
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                }else
                {
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like));
                }
            }
        });

        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(!task.getResult().exists())
                        {
                            HashMap<String,Object> likesMap = new HashMap<>();

                            likesMap.put("timestamp" , FieldValue.serverTimestamp());

                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(mAuth.getCurrentUser().getUid()).set(likesMap);

                        }else
                        {
                            //if user presess the like button again remove his like
                            firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(mAuth.getCurrentUser().getUid()).delete();
                        }

                    }
                });


            }
        });

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent commentActivity = new Intent(context,CommentActivity.class);
                commentActivity.putExtra("blog_post_id",blogPostId);
                context.startActivity(commentActivity);

            }
        });



    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {

        private View mView;

        private TextView descView;
        private ImageView blogImageView;
        private TextView blogDate;
        private TextView blogUsername;
        private CircleImageView blogUserProfile;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private TextView blogCommentCount;
        private Button deletePost;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_btn);
            deletePost = mView.findViewById(R.id.delete_post);
        }

        public void setDescText(String descText)
        {
            descView = mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri, String thumbUri){

            blogImageView = mView.findViewById(R.id.blog_image);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.grey_blog_image);

            Glide.with(context).applyDefaultRequestOptions(requestOptions).load(downloadUri).thumbnail(
                    Glide.with(context).load(thumbUri)
            ).into(blogImageView);

        }

        public void setTime(String date) {
            blogDate = mView.findViewById(R.id.blog_post_date);
            blogDate.setText(date);
        }

        public void setUserData(String username,String imageuri)
    {
        blogUsername = mView.findViewById(R.id.blog_username);
        blogUserProfile = mView.findViewById(R.id.blog_user_image);

        blogUsername.setText(username);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.grey_blog_profile);

        Glide.with(context).applyDefaultRequestOptions(requestOptions).load(imageuri).into(blogUserProfile);

    }

        public void updateLikesCount(int count)
        {
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count + " Likes");

        }

        public void updateCommentsCount(int count)
        {
            blogCommentCount = mView.findViewById(R.id.blog_comment_count);
            blogCommentCount.setText(count + " Comments");

        }
    }//viewholder
}
