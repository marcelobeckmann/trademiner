<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:rmdoc="com.rapidminer.gui.OperatorDocumentationBrowser">
<!-- <xsl:script implements-prefix="pref" language="java" src="java:com.rapid_i.test.OperatorDocToHtmlConverter" / -->
	<xsl:template match="/">
		<html>
			<head>
				<!-- <style type="text/css">
					h2
					{font-family: Arial;}
					h3
					{font-family: Arial; color: #3399FF;}
					.HeadIcon
					{height: 40px; width: 40px}
					p
					{padding: 0px 20px 1px 20px; font-family: Arial; /*font-size: 75%/*}
					ul
					{font-family: Arial; /*font-size: 75%*/}
					td
					{font-family: Arial/*; font-size: 75%*/; vertical-align: top}
				</style> -->
			</head>
			<body>	
			  <xsl:apply-templates />		
			</body>
		</html>
	</xsl:template>
	<xsl:template match="title">
		
	</xsl:template>
	<xsl:template match="operator">
		<xsl:variable name="operatorKey">
			<xsl:value-of select="@key" />
		</xsl:variable>
			<table>
				<tr>
					<td>
						<img>
							<xsl:attribute name="src"><xsl:value-of select="rmdoc:getIconNameForOperator($operatorKey)" /></xsl:attribute>
							<xsl:attribute name="Class">HeadIcon</xsl:attribute>
						</img>
					
					</td>
					<td valign="middle" align="center">
						<h2><xsl:value-of select="title" /></h2>
					</td>
				</tr>
			</table>
			<hr />
			<xsl:apply-templates />
		</xsl:template>
	<xsl:template match="synopsis">
		<h3>Synopsis</h3>
		<p>
			<xsl:apply-templates />
		</p>
	</xsl:template>
	<xsl:template match="text">
		<h3>Description</h3>
	 	<xsl:for-each select="paragraph">
			<p>
				<xsl:value-of select="." />
				<xsl:apply-templates select="ul" />
				<xsl:apply-templates select="p" />
			</p>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="em">
		<em><xsl:value-of select="." /></em>
	</xsl:template>
	
	<xsl:template match="inputPorts">
		<h3>Input</h3>
	
		<table border="0" cellspacing="7">
			<xsl:for-each select="port">
				<tr>
					<td>
						<table>
							<tr>
								<td class="lilIcon">
									<img><xsl:attribute name="src"><xsl:value-of select="rmdoc:getIconNameForType(@type)"/></xsl:attribute>
									<xsl:attribute name="class">typeIcon</xsl:attribute></img>
								</td>
								<td> 
									<b>
							 			<xsl:value-of select="rmdoc:insertBlanks(@name)" />
									</b>
									<i>
										<xsl:value-of select="rmdoc:getTypeNameForType(@type)"/>
									</i>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="."/>
					</td>
				</tr>
			</xsl:for-each>
		</table>
		
	</xsl:template>
	
	<xsl:template match="outputPorts">
		<h3>Output</h3>
		
		<table border="0" cellspacing="7">
			<xsl:for-each select="port">
				<tr>
					<td>
						<table>
							<tr>
								<td class="lilIcon">
									<img><xsl:attribute name="src"><xsl:value-of select="rmdoc:getIconNameForType(@type)"/></xsl:attribute>
									<xsl:attribute name="class">typeIcon</xsl:attribute></img>
								</td>
								<td> 
									<b>
							 			<xsl:value-of select="rmdoc:insertBlanks(@name)" />
									</b>
									<i>
										<xsl:value-of select="rmdoc:getTypeNameForType(@type)"/>
									</i>
								</td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="."/>
					</td>
				</tr>
			</xsl:for-each>
		</table>
		
	</xsl:template>
	<xsl:template match="parameters">
		<h3>Parameters</h3>
		<table border="0" cellspacing="7">
			<xsl:for-each select="parameter">
				<tr>
					<td width="75">
						<b>
							<xsl:value-of select="rmdoc:insertBlanks(@key)" />
						</b>
						<i>
							<xsl:value-of select="rmdoc:expert(@key)" />
						</i>
					</td>
				</tr>
				<tr>
					<td>
						<xsl:value-of select="."/>
						<b> Range: </b> <i> <xsl:value-of select="@type" /> </i>
					</td>
				</tr>
			</xsl:for-each>
		</table>
	</xsl:template>
	<xsl:template match="ul">
		<ul>
			
	 		<xsl:for-each select="li">
				<li>
					<xsl:value-of select="." />
				</li>
			</xsl:for-each>
		</ul>
	</xsl:template>
	<xsl:template match="tutorialProcesses">
		<h3>Tutorial Process</h3>
			<xsl:for-each select="tutorialProcess">
				<p>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="rmdoc:linkIncrement()" /></xsl:attribute>
						<xsl:value-of select="@title" />
					</a>
				</p>
				<xsl:for-each select="description/paragraph">
					<p>
						<xsl:value-of select="." />
					</p>
				</xsl:for-each>
			</xsl:for-each>
	</xsl:template>


		
</xsl:stylesheet>