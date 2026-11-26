package br.local.busca
import com.arm.aichat.NativeToolSchema
import kotlinx.coroutines.CancellationException
import org.json.JSONArray
import org.json.JSONObject

internal class LocalTool(val name:String,val description:String,val execute:suspend (JSONObject)->String){
    val schema="""{"type":"object","properties":{"query":{"type":"string","minLength":1,"maxLength":300}},"required":["query"],"additionalProperties":false}"""
}
internal class LocalMcpServer(tools:List<LocalTool>){
    private val registry=tools.associateBy{it.name}
    fun definitions()=registry.values.map{NativeToolSchema(it.name,it.description,it.schema)}
    suspend fun dispatch(request:JSONObject):JSONObject {
        val response=JSONObject().put("jsonrpc","2.0").put("id",request.opt("id")?:JSONObject.NULL)
        fun error(code:Int,message:String)=response.put("error",JSONObject().put("code",code).put("message",message))
        if(request.optString("jsonrpc")!="2.0")return error(-32600,"Requisição inválida")
        when(request.optString("method")){
            "initialize" -> return response.put("result",JSONObject().put("protocolVersion","2025-11-25").put("capabilities",JSONObject().put("tools",JSONObject())).put("serverInfo",JSONObject().put("name","buscalocal").put("version","0.5.1")))
            "tools/list" -> return response.put("result",JSONObject().put("tools",JSONArray(registry.values.map{JSONObject().put("name",it.name).put("description",it.description).put("inputSchema",JSONObject(it.schema))})))
            "tools/call" -> {
                val params=request.optJSONObject("params")?:return error(-32602,"Parâmetros ausentes")
                val tool=registry[params.optString("name")]?:return error(-32602,"Ferramenta não autorizada")
                val args=params.optJSONObject("arguments")?:return error(-32602,"Argumentos ausentes")
                val q=args.opt("query")
                if(args.length()!=1||q !is String||q.isBlank()||q.length>300)return error(-32602,"Consulta inválida")
                var failed=false
                val value=try{tool.execute(args)}catch(e:CancellationException){throw e}catch(e:Exception){failed=true;e.message?.take(250)?:"Falha da ferramenta"}
                return response.put("result",JSONObject().put("isError",failed).put("content",JSONArray().put(JSONObject().put("type","text").put("text",value.take(650)))))
            }
            else -> return error(-32601,"Método não disponível")
        }
    }
}