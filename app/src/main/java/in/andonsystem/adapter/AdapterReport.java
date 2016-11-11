package in.andonsystem.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.andonsystem.R;
import in.andonsystem.model.Downtime;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Md Zahid Raza on 26/06/2016.
 */
public class AdapterReport extends RecyclerView.Adapter<HolderReport> {

    private List<Downtime> list;
    private Context context;

    public AdapterReport(Context context,List<Downtime> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public HolderReport onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_report,parent,false);
        HolderReport holder = new HolderReport(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(HolderReport holder, int position) {
        Downtime data = list.get(position);

        String line = "line " + data.getLine();

        holder.icon.setLetter(data.getProbName().charAt(0));
        holder.icon.setOval(true);
        holder.probName.setText(data.getProbName());
        holder.line.setText(line);
        holder.deptName.setText(data.getDeptName());
        int min = data.getDowntime();
        String downtime;
        if(min >= 0) {
            int hour = (int) TimeUnit.MINUTES.toHours(min);
            min = min - (int) TimeUnit.HOURS.toMinutes(hour);

            if (hour > 0) {
                downtime = String.format("%02d hour %02d min", hour, min);
            } else {
                downtime = String.format("%02d min", min);
            }
        }else {
            downtime = "open";
        }
        holder.downtime.setText(downtime);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }
}
