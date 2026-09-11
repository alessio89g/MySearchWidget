package com.alessio89g.mysearchwidget;

public class ShortcutFixtureActivity extends android.app.Activity {
 @Override public void onCreate(android.os.Bundle state) {
  super.onCreate(state);
  if(getIntent().getBooleanExtra("publish",false)) {
   android.content.pm.ShortcutInfo shortcut=new android.content.pm.ShortcutInfo.Builder(this,"dynamic_precise")
    .setShortLabel("Dynamic precise")
    .setActivity(new android.content.ComponentName(getPackageName(),"com.alessio89g.mysearchwidget.ShortcutFixtureAlias"))
    .setIntent(new android.content.Intent(this,PrivateShortcutFixtureActivity.class).setAction("com.alessio89g.TEST_DYNAMIC"))
    .build();
   android.content.pm.ShortcutInfo second=new android.content.pm.ShortcutInfo.Builder(this,"dynamic_second")
    .setShortLabel("Dynamic second")
    .setActivity(new android.content.ComponentName(getPackageName(),"com.alessio89g.mysearchwidget.ShortcutFixtureAlias"))
    .setIntent(new android.content.Intent(this,PrivateShortcutFixtureActivity.class).setAction("com.alessio89g.TEST_SECOND"))
    .build();
   getSystemService(android.content.pm.ShortcutManager.class).setDynamicShortcuts(java.util.Arrays.asList(shortcut,second));
  }
  android.widget.TextView view=new android.widget.TextView(this);
  view.setText("Scorciatoia eseguita: "+getIntent().getAction()+" / "+getIntent().getStringExtra("mode"));
  setContentView(view);
 }
}
