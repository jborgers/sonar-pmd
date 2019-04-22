
# CallSuperLast
**Category:** `pmd`<br/>
**Rule Key:** `pmd:CallSuperLast`<br/>


-----

Super should be called at the end of the method. Example :
<pre>
public class DummyActivity extends Activity {
  public void onPause() {
    foo();
    // missing call to super.onPause()
  }
}
</pre>

