# A*

Implementation of the A* pathfinding algorithm in Java with a graphical visualization

[![java-2021-01-06-17-47-10.png](https://i.postimg.cc/s2rfVkgZ/java-2021-01-06-17-47-10.png)](https://postimg.cc/YvdBRygp)

## Settings

You can use the [variables in AStar](https://github.com/nkilders/a-star/blob/6916c2cb60009f0b0c761609de044790bb2c23e7/src/de/nkilders/astar/AStar.java#L11-L18) to change
- the grid size,
- the delay between the steps,
- how much percent of the grid should be barriers and
- whether diagonal steps are allowed.

```java
// ====== SETTINGS ====== //
final int nodeSize = 15; // for rendering
final int gridWidth = 50;
final int gridHeight = 50;
final long sleepTime = 5L;
final float barriers = 0.25F;
final boolean allowDiagonals = true;
// ====================== //
```