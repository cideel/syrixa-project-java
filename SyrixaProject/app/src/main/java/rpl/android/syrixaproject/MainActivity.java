package rpl.android.syrixaproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.Firebase;

import rpl.android.syrixaproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container_host);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.bottomNavigationBar, navController);

        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                switch (destination.getId()) {
                    case R.id.homeFragment:
                        visible();
                        break;
                    case R.id.profileFragment:
                        visible();
                        break;
                    case R.id.groupFragment:
                        visible();
                        break;
                    case R.id.addProjectFragment:
                        visible();
                        break;
                    default:
                        invisible();
                        break;
                }
            }
        });

    }

    private void visible(){
        binding.bottomNavigationBar.setVisibility(View.VISIBLE);
    }

    private void invisible(){
        binding.bottomNavigationBar.setVisibility(View.INVISIBLE);
    }


}