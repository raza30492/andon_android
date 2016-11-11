package in.andonsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import in.andonsystem.IssueDetailActivity;
import in.andonsystem.R;
import in.andonsystem.model.Problem;

import java.util.TreeSet;

/**
 * Created by Md Zahid Raza on 25/06/2016.
 */
public class AdapterHome extends RecyclerView.Adapter<HolderHome> {

    private Context context;
    private TreeSet<Problem> set;

    public AdapterHome(Context context, TreeSet<Problem> set){
        this.context = context;
        this.set = set;
    }

    @Override
    public HolderHome onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home,parent,false);
        LinearLayout container = (LinearLayout) view.findViewById(R.id.issue_container);

        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idStr = ((TextView)v.findViewById(R.id.issue_id)).getText().toString();
                Intent i = new Intent(context, IssueDetailActivity.class);
                i.putExtra("issueId",Integer.parseInt(idStr));
                context.startActivity(i);
            }
        });
        if(viewType == 0){
            container.setBackgroundColor(ContextCompat.getColor(context,R.color.tomato));
        }else if(viewType == 1){
            container.setBackgroundColor(ContextCompat.getColor(context,R.color.blue));
        }else if(viewType == 2){
            container.setBackgroundColor(ContextCompat.getColor(context,R.color.limeGreen));
        }

        HolderHome holder = new HolderHome(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(HolderHome holder, int position) {

        Object[] array = set.toArray();
        Problem issue = (Problem)array[position];
        String probName = issue.getProbName();
        if(issue.getCritical().equals("YES")){
            probName += "*";
        }
        holder.icon.setLetter(issue.getProbName().charAt(0));
        holder.icon.setOval(true);
        holder.probName.setText(probName);
        holder.time.setText(issue.getRaiseTime());
        holder.deptName.setText(issue.getDeptName());
        if(issue.getDowntime() >= 0){
            String downtime;
            int down = issue.getDowntime();
            if(down >= 100){
                downtime = "["+String.format("%03d",issue.getDowntime()) +" min]    "+issue.getLine();
            }else{
                downtime = "[ "+String.format("%02d",issue.getDowntime()) +" min ]    "+issue.getLine();
            }
            holder.line.setText(downtime);
        }else{
            holder.line.setText(issue.getLine());
        }
        holder.issueId.setText(String.valueOf(issue.getIssueId()));

    }

    @Override
    public int getItemCount() {
        return set.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object[] array = set.toArray();
        Problem issue = (Problem)array[position];
        return issue.getFlag();
    }

    public void insert(Problem issue){
        set.add(issue);
        notifyDataSetChanged();
    }

    public void update(Problem issue){
        Problem temp = new Problem();
        temp.setIssueId(issue.getIssueId());
        temp.setFlag((issue.getFlag() - 1));
        set.remove(temp);
        temp.setFlag(issue.getFlag()-2);
        set.remove(temp);
        set.add(issue);
        notifyDataSetChanged();
    }
}
