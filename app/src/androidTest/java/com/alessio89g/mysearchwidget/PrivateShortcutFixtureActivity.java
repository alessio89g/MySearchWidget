package com.alessio89g.mysearchwidget;
public class PrivateShortcutFixtureActivity extends android.app.Activity {
 @Override public void onCreate(android.os.Bundle state) {
  super.onCreate(state);
  android.widget.TextView view=new android.widget.TextView(this);
  view.setText("Pinned dynamic shortcut launched");setContentView(view);
 }
}
