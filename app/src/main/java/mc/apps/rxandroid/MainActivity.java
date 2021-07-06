package mc.apps.rxandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.concurrent.Flow;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.observers.BlockingBaseObserver;
import io.reactivex.rxjava3.observers.DefaultObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tests";
    private static final long OBSERVABLE_TIME_SLEEP = 10 ;
    private static final long OBSERVERS_TIME_SLEEP = 500 ;
    private static final int OBSERV_MAX_ITEMS = 50;
    private TextView title;
    private Button start, stop;

    Observable<Integer> testObservable;
    CompositeDisposable disposables;

    RecyclerView inputRecyclerView, outputRecyclerView;
    ItemsListAdapter adapter;
    private final LinkedList<String> items = new LinkedList<>();
    private final LinkedList<String> items2 = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = findViewById(R.id.title);
        start = findViewById(R.id.btnStart);
        stop = findViewById(R.id.btnStop);

        start.setOnClickListener((e)->defineObserver());
        stop.setOnClickListener((e)->stopObserver());

        disposables = new CompositeDisposable();

        InitLists();

        //setObserver();
    }

    private void InitLists() {
        inputRecyclerView = findViewById(R.id.input);
        outputRecyclerView = findViewById(R.id.output);

        adapter = new ItemsListAdapter(this, items,true);
        inputRecyclerView.setAdapter(adapter);

        adapter = new ItemsListAdapter(this, items2, false);
        outputRecyclerView.setAdapter(adapter);

        inputRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        outputRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private void updateList(int list, String item, boolean add) {
        runOnUiThread(() -> {
            if(list==1) {
                if (add) {
                    items.addFirst(item);
                    title.setText("Observable - Send : "+item);
                    title.setTextColor(Color.YELLOW);
                }else
                    items.remove(item);
                inputRecyclerView.getAdapter().notifyDataSetChanged();
            }else{
                if (add) {
                    items2.addFirst(item);
                    title.setText("Subscriber - Handle : "+item);
                    title.setTextColor(Color.GREEN);
                }else
                    items2.remove(item);
                outputRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });

    }
    private void stopObserver() {
        if(testObservable !=null) {

            //testObservable.unsubscribeOn(Schedulers.io());
            disposables.clear();

            start.setEnabled(true);
            stop.setEnabled(false);

            resetItems();
        }
    }
    private void defineObserver() {
        start.setEnabled(false);
        stop.setEnabled(true);

        resetItems();

        //TODO  : + publisher...
        testObservable = Observable.range(1,OBSERV_MAX_ITEMS);
        testObservable
                .doOnNext(x->{
                    safeSleep(OBSERVABLE_TIME_SLEEP*ThreadLocalRandom.current().nextInt(1, 10));
                    updateList(1, x.toString(),true);
                })
                .subscribeOn(Schedulers.io())
                .doOnError(this::handleError)
                .doOnComplete(this::handleComplete)
                .doOnSubscribe(disposable -> disposables.add(disposable))
                .observeOn(Schedulers.computation())
                .subscribe(this::observerTask);
    }

    private void observerTask(Integer value) {
        safeSleep(OBSERVERS_TIME_SLEEP);
        updateList(2, value + "", true);
        updateList(1, value + "", false);
    }

    private void setObserver(){
        Observable<Long> fastObservable = Observable.interval(1, TimeUnit.MILLISECONDS);
        Observable<Long> slowObservable = Observable.interval(1, TimeUnit.SECONDS);

        Disposable disposable = slowObservable
                .subscribeOn(Schedulers.io())           // Schedulers.io() : pool de threads illimité
                .map(x->{
                    Log.i(TAG, "Observable: " + x);
                    updateList(1, x.toString(),true);
                    return x;
                })
                .observeOn(Schedulers.computation())    // Schedulers.computation() : pool de threads limité (nombre de processeurs disponibles)
                .subscribe(x -> {
                            updateList(2, x.toString(),true);
                            updateList(1, x.toString(),false);
                            safeSleep(1500);
                            Log.i(TAG, "\tObserver: " + x);
                        });

        safeSleep(10000);
        disposable.dispose();

       // Observables.zip(fastObservable, slowObservable)
    }

    private void safeSleep(long i) {
        // SystemClock.sleep
        try {
            Thread.sleep(i);
        }catch (InterruptedException e){}
    }

    private void resetItems() {
        runOnUiThread(() -> {

            ((ItemsListAdapter)inputRecyclerView.getAdapter()).reset();
            ((ItemsListAdapter)outputRecyclerView.getAdapter()).reset();
            inputRecyclerView.getAdapter().notifyDataSetChanged();
            outputRecyclerView.getAdapter().notifyDataSetChanged();

        });
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }

    private void handleError(Throwable throwable) {
        runOnUiThread(() -> {
            title.setText("Observable : Error = "+throwable);
        });
    }
    private void handleComplete() {
        runOnUiThread(() -> {
            title.setText("Observable : Completed!");
        });
    }
//    private void updateText(Integer value) {
//        runOnUiThread(() -> {
//
//            title.setText("Subscriber - Handle : "+value);
//            this.updateColor(value);
//
//            updateList(2, value.toString(),true);
//        });
//    }
//    private void updateColor(Integer value) {
//        title.setTextColor(value%2==0? Color.GREEN:Color.YELLOW);
//    }
    /**
     * RecyclerView - Adapter
     */
    public class ItemsListAdapter extends   RecyclerView.Adapter<ItemsListAdapter.ItemViewHolder>  {
        private final LinkedList<String> items;
        private LayoutInflater mInflater;
        private boolean first;

        public ItemsListAdapter(Context context, LinkedList<String> items, boolean first) {
            mInflater = LayoutInflater.from(context);
            this.items = items;
            this.first = first;
        }

        public void reset() {
            this.items.clear();
        }

        @androidx.annotation.NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {
            View mItemView = mInflater.inflate(R.layout.item_layout, parent, false);
            return new ItemViewHolder(mItemView, this);
        }

        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ItemViewHolder holder, int position) {
            holder.title.setText(items.get(position)+" ("+position+")");

            if(first) {
                int DARK = ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimaryDark);
                holder.title.setBackgroundColor((position > 5) ? Color.RED : DARK);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
        class ItemViewHolder extends RecyclerView.ViewHolder {
            public final TextView title;
            final ItemsListAdapter mAdapter;

            public ItemViewHolder(@NonNull View itemView, ItemsListAdapter adapter) {
                super(itemView);
                title = itemView.findViewById(R.id.title);
                this.mAdapter = adapter;
            }
        }
    }
}