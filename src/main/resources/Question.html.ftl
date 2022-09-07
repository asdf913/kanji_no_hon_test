<!DOCTYPE html>
<html>
	<head>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/vue/1.0.28/vue.min.js"></script>
	</head>
	<body class="bg-dark">
		<div id="app">
			<input type="checkbox" v-model="shuffle"><slot v-if="shuffle">Shuffle</slot><slot v-if="!this.shuffle">Ordered</slot></input>
			<div/>
			<table>
				<thead>
					<tr>
						<th>Unit</th>
						<th>{{unit}}</th>
					</tr>
				</thead>
			</table>
			<table>
				<tbody>
					<tr v-for="question in questions">
						<td>{{question.chapter}}</td>
						<td><slot v-if="typeof question=='object'&&question!==null&&typeof question.fukushuu==='boolean'&&question.fukushuu">復習&nbsp;-&nbsp;</slot>{{question.section}}</td>
						<td>{{question.prefix!""}}</td>
						<td>
							<slot v-if="typeof question=='object'&&question!==null&&typeof question.texts==='object'&&question.texts!==null&&typeof question.texts.length==='number'">
								<slot v-for="item in question.texts">
									<slot v-if="typeof item.text==='string'">{{item.text}}</slot>
									<slot v-if="typeof item.answer==='string'&&item.answer!==null&&typeof item.answer.length==='number'&&item.answer.length>0">
										<input type="text" v-model="item.input" :class="item.input!==null&&item.input.length>0?(isCorrect(item)?'correct':'wrong'):''"/>
									</slot>
								</slot>
							</slot>
						</td>
					</tr>
				</tbody>
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
					 shuffle:null
					,unit:"4"
					,originalQuestions:[
						<#list questions as question>
							{
								 "chapter" :${question.chapter!"null"}
								,"section" :${question.section!"null"}
								,"prefix"  :<#if question.prefix??>"${question.prefix}"<#else>null</#if>
								,"fukushuu":
								<#if question?? && question.fukushuu?? && question.fukushuu?is_boolean>
								${question.fukushuu?string}
								<#else>${question.fukushuu!"null"}</#if>
								,"texts"  :
									<#if question.texts??>
										[
										<#list question.texts as text>
											{"text":"${text.text!""}","answer":"${text.answer!""}"}
											<#sep>,</#sep>
										</#list>
										]
									</#if>
							}
							<#sep>,</#sep>
						</#list>
					]
				},created:function(){
					this.show();
				},methods:{
					show:function(){
						//
						var questions=JSON.parse(JSON.stringify(this.originalQuestions));
						//
						//https://stackoverflow.com/questions/2450954/how-to-randomize-shuffle-a-javascript-array
						//
						if(typeof this.shuffle==="boolean"&&this.shuffle){
							questions= questions.map(value => ({value,sort:Math.random()}))
								      .sort((a, b)=>a.sort-b.sort)
								      .map(({value})=>value);
						}
						//
						Vue.set(this,"questions",questions);
						//
						this.clearAnswers();
						//
					},clearAnswers:function(){
						for(var i=0;this.questions!==null&&i<this.questions.length;i++){
							Vue.set(this.questions[i],"input","");
						}
					},isCorrect:function(item){
						return item.answer===item.input;
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