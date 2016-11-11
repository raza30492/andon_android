package in.andonsystem.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import in.andonsystem.R;
import in.andonsystem.view.LetterImageView;

/**
 * Created by Md Zahid Raza on 23/06/2016.
 */
public class HolderNotification extends RecyclerView.ViewHolder {

    LetterImageView icon;
    TextView message;
    TextView time;
    TextView id;

    public HolderNotification(View view){
        super(view);

        icon = (LetterImageView)view.findViewById(R.id.nfn_letter_image);
        message = (TextView) view.findViewById(R.id.nfn_message);
        time = (TextView)view.findViewById(R.id.nfn_time);
        id = (TextView) view.findViewById(R.id.issue_id);

    }
}
