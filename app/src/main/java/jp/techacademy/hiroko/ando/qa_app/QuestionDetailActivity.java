package jp.techacademy.hiroko.ando.qa_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {
    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private  FloatingActionButton bookmark;//課題
    private ProgressDialog mProgress;//課題
    private FirebaseUser user;//課題
    private boolean Fabflag = false ;//課題
    private int mGenre;
    private DatabaseReference dataBaseReference;
    private DatabaseReference mAnswerRef;
    private DatabaseReference fabRef;


    private ChildEventListener mFabReserchListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                bookmark.setImageResource(R.drawable.hart2);
                Fabflag = true;

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for (Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        // 渡ってきたジャンルの番号を保持する
        mGenre = extras.getInt("genre");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        //floating Button　課題
        FloatingActionButton fab = findViewById(R.id.fab);
        bookmark = findViewById(R.id.bookmark);

        //リファレンス　課題
         dataBaseReference = FirebaseDatabase.getInstance().getReference();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }



    @Override
    protected void onResume(){
        super.onResume();

       user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // ログインしていなければ
            bookmark.setVisibility(View.INVISIBLE);
            bookmark.setEnabled(false);
        } else {
            //
            bookmark.setVisibility(View.VISIBLE);
            bookmark.setEnabled(true);

            fabRef = dataBaseReference.child(Const.FavoritesPATH).child(user.getUid()).child(mQuestion.getQuestionUid());
            fabRef.addChildEventListener(mFabReserchListener);

            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(Fabflag){
                        //データ削除
                        fabRef.removeValue();
                        bookmark.setImageResource(R.drawable.hart);
                        Fabflag = false;
                    }else{
                        Map<String, String> fabdata = new HashMap<String, String>();
                        fabdata.put("Genre", String.valueOf(mQuestion.getGenre()));
                        fabRef.setValue(fabdata);
                    }

                }
            });

        }
    }

}
