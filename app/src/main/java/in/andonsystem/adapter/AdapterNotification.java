package in.andonsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import in.andonsystem.IssueDetailActivity;
import in.andonsystem.R;
import in.andonsystem.model.Notification;

import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by Md Zahid Raza on 23/06/2016.
 */
public class AdapterNotification extends RecyclerView.Adapter<HolderNotification> {


    private TreeSet<Notification> set;
    private Context context;

    public AdapterNotification(Context context, TreeSet<Notification> set){
        this.context = context;
        this.set = set;
    }

    @Override
    public HolderNotification onCreateViewHolder(ViewGroup parent, int viewType) {

        View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notification,parent,false);
        RelativeLayout container = (RelativeLayout) view.findViewById(R.id.issue_container);
        container.setOnClickListener(new RelativeLayout.OnClickListener(){
            @Override
            public void onClick(View v) {
                String idStr = ((TextView)v.findViewById(R.id.issue_id)).getText().toString();
                Intent i = new Intent(context, IssueDetailActivity.class);
                i.putExtra("issueId",Integer.parseInt(idStr));
                context.startActivity(i);
            }
        });

        HolderNotification holder = new HolderNotification(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(HolderNotification holder, int position) {
        Object[] array = set.toArray();
        Notification data = (Notification)array[position];

        long time = data.getTime();
        int hours,mins;
        hours = (int)TimeUnit.MILLISECONDS.toHours(time);
        time = time - TimeUnit.HOURS.toMillis(hours);
        mins = (int)TimeUnit.MILLISECONDS.toMinutes(time);
        String at;
        if(hours > 0) {
            at = String.format("%02d hour %02d min ago", hours, mins);
        }else{
            at = String.format("%02d min ago",mins);
        }

        holder.message.setText(data.getMessage());
        holder.id.setText(String.valueOf(data.getIssueId()));
        holder.time.setText(at);

        if(data.getFlag() == 0){
            holder.icon.setLetter('R');
            holder.icon.setBackgroundColor("#ff4444");
        }else if(data.getFlag() == 1){
            holder.icon.setLetter('A');
            holder.icon.setBackgroundColor("#0099cc");
        }else{
            holder.icon.setLetter('S');
            holder.icon.setBackgroundColor("#669900");
        }

    }

    @Override
    public int getItemViewType(int position) {
        Object[] array = set.toArray();
        Notification data = (Notification)array[position];
        return data.getFlag();
    }

    @Override
    public int getItemCount() {
        return set.size();
    }

}
