package com.alessio89g.mysearchwidget.data

/** Immutable snapshots share their assets; history is bounded and stays in the editor. */
data class EditHistory<T>(val present:T,val past:List<T> = emptyList(),val future:List<T> = emptyList()) {
 fun record(value:T)=if(value==present)this else EditHistory(value,(past+present).takeLast(100),emptyList())
 fun undo()=if(past.isEmpty())this else EditHistory(past.last(),past.dropLast(1),(listOf(present)+future).take(100))
 fun redo()=if(future.isEmpty())this else EditHistory(future.first(),(past+present).takeLast(100),future.drop(1))
}
