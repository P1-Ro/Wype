package sk.p1ro.wype.utils;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

import static androidx.test.espresso.action.ViewActions.click;

public class ViewActions {


    public static ViewAction clickChildwWithId(final int id) {
        return new ViewAction() {
            ViewAction click = click();

            @Override
            public Matcher<View> getConstraints() {
                return click.getConstraints();
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                click.perform(uiController, view.findViewById(id));
            }
        };
    }


}
