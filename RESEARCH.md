Take anything in this documentation as "probably":


- visible on/off
    ```
    <!-- uientry flag 8 -->
    ```

- movable on/off
    ```
    <!-- uientry flag 4 -->
    ```

- turn visibilty of state text on/off ? 
  
  this is required to be set to yes if you want to change state text otherwise its "dy"
  ```
  <!-- state stuff 4? -->
  ```

- sets(probably) interactivity of state
  ```
  <s>Default Font Category</s><!-- font category / twui -->
  <i>0</i><!-- left ? -->
  <i>0</i><!-- right ? -->
  <i>0</i><!-- top ? -->
  <i>0</i><!-- bottom ? -->
  <i>0</i>
  <yes />                      <<<<<<<<<<<<<<<<<<<<<<<<<<<<<< this one   
  ```

- some kind of size based behaviour based on children or parent?
  can make the element shorter or wider
  ```
  <no /><!-- uientry flag 1 -->
  ```

- when minimizing also minimizes all children?
  does not allow children to overflow parent?
  ```
  <no /><!-- uientry flag 9 -->
  ```
