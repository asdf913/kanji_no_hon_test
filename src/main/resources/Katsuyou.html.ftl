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
						<td style="white-space:nowrap;" v-for="conjugation in conjugations">{{conjugation}}</td>
					</tr>
				</thead>
				<tbody>
					<tr v-for="verb in verbs">
						<td style="white-space:nowrap;" v-for="conjugation in conjugations">
							<slot v-if="conjugationShown==conjugation">{{verb[conjugation]}}</slot>
							<slot v-else>
								<input type="text" v-model="verb.input[conjugation]" :class="verb.input[conjugation]!==null&&typeof verb.input[conjugation].length==='number'&&verb.input[conjugation].length>0?(verb[conjugation]==verb.input[conjugation]?'correct':'wrong'):''"/>
							</slot>
						</td>
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
					,conjugationShown:"${conjugationShown}"
					,conjugations:[<#list conjugations as conjugation>"${conjugation}"<#sep>,</#sep></#list>]
					,originalVerbs:[
						<#list verbs as verb>
							<#if verb?? && verb.conjugations??>
								{
								<#list verb.conjugations?keys as conjugation>
									"${conjugation}":"${verb.conjugations[conjugation]}"
									<#sep>,</#sep>								
								</#list>
								}
							</#if>
							<#sep>,</#sep>
						</#list>
					]
				},watch:{
					shuffle:{
						handler:function(){this.show();}
					}
				},created:function(){
					this.show();
				},methods:{
					show:function(){
						//
						var verbs=JSON.parse(JSON.stringify(this.originalVerbs));
						//
						//https://stackoverflow.com/questions/2450954/how-to-randomize-shuffle-a-javascript-array
						//
						if(typeof this.shuffle==="boolean"&&this.shuffle){
							verbs= verbs.map(value => ({value,sort:Math.random()}))
								  .sort((a, b)=>a.sort-b.sort)
								  .map(({value})=>value);
						}
						//
						Vue.set(this,"verbs",verbs);
						//
						this.clearAnswers();
						//
					},clearAnswers:function(){
						for(var i=0;this.verbs!==null&&i<this.verbs.length;i++){
							Vue.set(this.verbs[i],"input",{});
						}
					}
				}
			})
		</script>
	</body>
</html>