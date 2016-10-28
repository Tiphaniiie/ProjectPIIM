package telecom.projectpiim;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class Analysis extends AppCompatActivity {
    ImageView imgResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        this.imgResult = (ImageView) this.findViewById(R.id.imgResult);
    }

}
