package jp.techacademy.hiroko.ando.qa_app;


import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FabActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mFavRef;
    private DatabaseReference mContentRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;
    private ArrayList<String> FavIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fab);

        mToolbar = findViewById(R.id.fabtoolbar);
        mToolbar.setTitle("お気に入り");

        //  戻るボタン実装
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = findViewById(R.id.fablistView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        FavIdList = new ArrayList<>();
        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        // 選択したジャンルにリスナーを登録する
        if (mFavRef != null) {
            mFavRef.removeEventListener(mFavListener);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mFavRef = mDatabaseReference.child(Const.FavoritesPATH).child(user.getUid());
        mFavRef.addChildEventListener(mFavListener);

    }

    private ChildEventListener mFavListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            final String genre =(String)map.get("Genre");
            String qestionuid = dataSnapshot.getKey();

            mContentRef = mDatabaseReference.child(Const.ContentsPATH).child(genre).child(qestionuid);

            mContentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    HashMap map = (HashMap) snapshot.getValue();
                    String title = (String) map.get("title");
                    String body = (String) map.get("body");
                    String name = (String) map.get("name");
                    String uid = (String) map.get("uid");
                    String imageString = (String) map.get("image");
                    byte[] bytes;
                    if (imageString != null) {
                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                    } else {
                        bytes = new byte[0];
                    }

                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            answerArrayList.add(answer);
                        }
                    }

                    Question question = new Question(title, body, name, uid, snapshot.getKey(), Integer.parseInt(genre), bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    //戻るボタン
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
