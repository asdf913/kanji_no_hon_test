<!DOCTYPE html>
<html>
	<head>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/vue/1.0.28/vue.min.js"></script>
	</head>
	<body class="bg-dark">
		<div id="app">
			<button @click="show('hiragana')" :class="this.mode=='hiragana'?'highlight':''">hiragana</button>
			<button @click="show('text')"     :class="this.mode=='text'    ?'highlight':''">text</button>
			<input type="checkbox" v-model="shuffle"><slot v-if="shuffle">Shuffle</slot><slot v-if="!this.shuffle">Ordered</slot></input>
			<div/>
			<table>
				<thead v-if="mode!==null&&mode.length>0">
					<tr>
						<th>Unit</th>
						<th>{{unit}}</th>
					</tr>
				</thead>
				<tbody v-if="mode==='hiragana'">
					<tr v-for="item in items" :class="item.answer!==null&&item.answer.length>0?(isCorrect(item)?'correct':'wrong'):''">
						<td colspan="2">{{item.text}}</td>	
						<td>{{item.hint}}</td>
						<td><input type="text" v-model="item.answer"/></td>
						<td><span v-if="item.answer!==null&&item.answer.length>0">{{isCorrect(item)?'&#x2714;':'&#x2718;'}}</span></td>
					</tr>
				</tbody>
				<tbody v-if="mode==='text'">
					<tr v-for="item in items" :class="item.answer!==null&&item.answer.length>0?(isCorrect(item)?'correct':'wrong'):''">
						<td colspan="2">{{item.hiragana}}</td>
						<td><input type="text" v-model="item.answer"/></td>
						<td><span v-if="item.answer!==null&&item.answer.length>0">{{isCorrect(item)?'&#x2714;':'&#x2718;'}}</span></td>
					</tr>
				</tbody>
				<tbody v-if="mode!==null&&mode.length>0">
					<tr>
						<td colspan="3">&nbsp;</td>
						<td>{{showCorrectCount()}}/{{items.length}}</td>
					</tr>
				</tbody>
			</table>
		</div>
		<style>
			.correct{background-color:lightgreen}
			.wrong  {background-color:lightpink}
			.highlight{background-color:yellow}
		</style>
		<script>
			new Vue({
				el:"#app"
				,data:{
					 mode:null
					,shuffle:null
					,unit:"4"
					,orginalItems:[<#list texts as text>{"text":"${text.text}","hiragana":"${text.hiragana}","hint":"${text.hint!""}"}<#sep>,</#sep></#list>]
				},methods:{
					show:function(mode){
						//
						this.mode=mode;
						//
						var items=JSON.parse(JSON.stringify(this.orginalItems));
						//
						//https://stackoverflow.com/questions/2450954/how-to-randomize-shuffle-a-javascript-array
						//
						if(typeof this.shuffle==="boolean"&&this.shuffle){
							items= items.map(value => ({value,sort:Math.random()}))
								  .sort((a, b)=>a.sort-b.sort)
								  .map(({value})=>value);
						}
						//
						Vue.set(this,"items",items);
						//
						this.clearAnswers();
						//
					},clearAnswers:function(){
						for(var i=0;this.items!==null&&i<this.items.length;i++){
							Vue.set(this.items[i],"answer","");
						}
					},isCorrect:function(item){
						if(item!==null){
							if(      this.mode==='hiragana'){return item.answer===item.hiragana;
							}else if(this.mode==='text'    ){return item.answer===item.text;}
						}
						return null;
					},showCorrectCount:function(){
						var result=0;
						var item=null;
						for(var i=0;this.items!==null&&i<this.items.length;i++){
							if(!this.isCorrect(this.items[i])){continue;}
							result+=1;
						}
						return result;
					}
				}
			})
		</script>
	</body>
</html>