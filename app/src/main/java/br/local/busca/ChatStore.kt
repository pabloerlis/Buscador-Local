package br.local.busca

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal data class Chat(val id:Long,val title:String)
internal data class Message(val role:String,val body:String)
internal data class SavedSource(val id:Long,val title:String,val url:String,val kind:String,val body:String,val query:String,val created:Long)

internal class ChatStore(context:Context):SQLiteOpenHelper(context,"research-chat.db",null,1) {
    override fun onConfigure(db:SQLiteDatabase){db.setForeignKeyConstraintsEnabled(true)}
    override fun onCreate(db:SQLiteDatabase){
        db.execSQL("CREATE TABLE chats(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,created INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT,chat INTEGER NOT NULL REFERENCES chats(id) ON DELETE CASCADE,role TEXT NOT NULL,body TEXT NOT NULL)")
        db.execSQL("CREATE TABLE sources(id INTEGER PRIMARY KEY AUTOINCREMENT,chat INTEGER NOT NULL REFERENCES chats(id) ON DELETE CASCADE,title TEXT NOT NULL,url TEXT NOT NULL,kind TEXT NOT NULL,body TEXT NOT NULL,query TEXT NOT NULL,created INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX messages_chat ON messages(chat,id)")
        db.execSQL("CREATE INDEX sources_chat ON sources(chat,id)")
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){error("Migração indisponível")}
    fun create():Long= writableDatabase.insertOrThrow("chats",null,ContentValues().apply{put("title","Nova pesquisa");put("created",System.currentTimeMillis())})
    fun rename(id:Long,title:String){writableDatabase.update("chats",ContentValues().apply{put("title",title.take(60))},"id=?",arrayOf(id.toString()))}
    fun chats():List<Chat> = readableDatabase.rawQuery("SELECT id,title FROM chats ORDER BY id DESC",null).use { c -> buildList {while(c.moveToNext())add(Chat(c.getLong(0),c.getString(1))) } }
    fun preview(chat:Long):String = readableDatabase.rawQuery("SELECT body FROM messages WHERE chat=? AND role IN ('user','search','assistant') ORDER BY id DESC LIMIT 1",arrayOf(chat.toString())).use{c->if(c.moveToFirst())c.getString(0).replace('\n',' ').take(140) else ""}
    fun sourceCount(chat:Long):Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM sources WHERE chat=?",arrayOf(chat.toString())).use{c->c.moveToFirst();c.getInt(0)}
    fun messages(chat:Long):List<Message> = readableDatabase.rawQuery("SELECT role,body FROM messages WHERE chat=? ORDER BY id",arrayOf(chat.toString())).use { c -> buildList {while(c.moveToNext())add(Message(c.getString(0),c.getString(1))) } }
    fun add(chat:Long,role:String,body:String){writableDatabase.insertOrThrow("messages",null,ContentValues().apply{put("chat",chat);put("role",role);put("body",body)})}
    fun sources(chat:Long):List<SavedSource> = readableDatabase.rawQuery("SELECT id,title,url,kind,body,query,created FROM sources WHERE chat=? ORDER BY id",arrayOf(chat.toString())).use { c -> buildList {while(c.moveToNext())add(SavedSource(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getLong(6))) } }
    fun save(chat:Long,hit:Hit,kind:String,body:String,query:String):Long {
        val content=body.take(24000)
        val found=readableDatabase.rawQuery("SELECT id FROM sources WHERE chat=? AND url=? AND kind=? AND body=? LIMIT 1",arrayOf(chat.toString(),hit.url.toString(),kind,content)).use{c->if(c.moveToFirst())c.getLong(0) else null}
        return found ?: writableDatabase.insertOrThrow("sources",null,ContentValues().apply{put("chat",chat);put("title",hit.title.take(300));put("url",hit.url.toString());put("kind",kind);put("body",content);put("query",query);put("created",System.currentTimeMillis())})
    }
}