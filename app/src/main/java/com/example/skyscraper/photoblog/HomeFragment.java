package com.example.skyscraper.photoblog;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blogHomeRecyclerView;
    private List<BlogPost> blogList;
    private List<User> userList;
    private BlogRecyclerAdapter blogRecyclerAdater;

    private FirebaseFirestore firebaseFirestore;

    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;

    private Context context;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home , container,false);

        blogList = new ArrayList<>();
        userList = new ArrayList<>();
        blogHomeRecyclerView = view.findViewById(R.id.blog_home_recycler);

        blogRecyclerAdater = new BlogRecyclerAdapter(blogList,userList);
        blogHomeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogHomeRecyclerView.setAdapter(blogRecyclerAdater);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {


            blogHomeRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){

                        context = container.getContext();
                       //Toast.makeText(container.getContext(),"reached bottom ",Toast.LENGTH_SHORT).show();

                        loadMorePost();

                    }

                }
            });

            firebaseFirestore = FirebaseFirestore.getInstance();

            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(3);


            firstQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (!documentSnapshots.isEmpty()) {

                        if (isFirstPageFirstLoad) {

                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            blogList.clear();
                            userList.clear();

                        }
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();
                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                String blogUserId = doc.getDocument().getString("user_id");

                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if(task.isSuccessful())
                                        {
                                            User user = task.getResult().toObject(User.class);


                                            if(isFirstPageFirstLoad)
                                            {
                                                userList.add(user);
                                                blogList.add(blogPost);

                                            }else{
                                                userList.add(0,user);
                                                blogList.add(0,blogPost);
                                            }
                                            blogRecyclerAdater.notifyDataSetChanged();
                                        }//taskSuccesfull
                                    }
                                });




                            }
                        }

                        isFirstPageFirstLoad = false;
                    }
                }
            });

        }else
        {

        }

        return view;
    }


    public void loadMorePost(){

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(3);

            nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    //Toast.makeText(context,"in loadmore ",Toast.LENGTH_SHORT).show();


                    if (!documentSnapshots.isEmpty()) {
                       // Toast.makeText(context,"snapshot not empty ",Toast.LENGTH_SHORT).show();


                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String blogPostId = doc.getDocument().getId();

                                final BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                                String blogUserId = doc.getDocument().getString("user_id");

                                firebaseFirestore.collection("Users").document(blogUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if(task.isSuccessful())
                                        {
                                            User user = task.getResult().toObject(User.class);

                                                userList.add(user);
                                                blogList.add(blogPost);


                                            blogRecyclerAdater.notifyDataSetChanged();
                                        }//taskSuccesfull
                                    }
                                });
                            }

                        }
                    }

                }
            });

        }

    }

}
